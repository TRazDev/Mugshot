package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Test
import java.io.File

/**
 * Up-to-date checks, the build cache, the configuration cache and re-run triggers.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class IncrementalBuildTest : MugshotPluginTestCase() {
  @Test
  fun prepareResourcesCaching() {
    val fixtureRoot = fixture("prepare-resources-task-caching")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = fixtureRoot.runBuild("testRelease", "testDebug", "--build-cache")

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotDebugResources", FROM_CACHE)

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotReleaseResources", FROM_CACHE)

    var resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    var resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("release") }).isFalse()

    resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/release/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("debug") }).isFalse()

    // delete now (regardless of future cleanup)
    buildDir.deleteRecursively()

    val secondRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE)

    resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("release") }).isFalse()
  }

  @Test
  fun cacheable() {
    val fixtureRoot = fixture("cacheable")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotDebugResources", FROM_CACHE)

    buildDir.deleteRecursively()

    val secondRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE)
  }

  @Test
  fun cacheableRelocatable() {
    val fixtureRoot = fixture("cacheable")
    fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotDebugResources", FROM_CACHE)
    firstRun.assertTaskOutcomeIsNot(":testDebugUnitTest", FROM_CACHE)

    // Rebuild the same project (with its populated cache) from a different directory, as CI and a
    // local clone would. Absolute paths in the cache key would make these entries unreachable here.
    val relocatedRoot = fixtureRoot.parentFile.resolve("cacheable-relocated").registerForDeletionOnExit()
    relocatedRoot.deleteRecursively()
    fixtureRoot.copyRecursively(relocatedRoot)
    relocatedRoot.resolve("build").deleteRecursively()
    // Pin the project name (fed into the Kotlin module name embedded in compiled classes) so it
    // stays constant across dirs; real CI-vs-local checkouts share the same leaf directory name.
    relocatedRoot.resolve("settings.gradle").let {
      it.writeText("rootProject.name = 'cacheable'\n${it.readText()}")
    }

    val secondRun = relocatedRoot.runBuild("testDebug", "--build-cache")

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE)
    secondRun.assertTaskOutcome(":testDebugUnitTest", FROM_CACHE)
  }

  @Test
  fun configurationCache() {
    val fixtureRoot = fixture("configuration-cache")

    // check to avoid plugin regressions that might affect Gradle's configuration caching
    // https://docs.gradle.org/current/userguide/configuration_cache.html
    fixtureRoot.runBuild("testDebug", "--configuration-cache")
  }

  @Test
  fun configurationCacheWorksWithGeneratedSources() {
    val fixtureRoot = fixture("configuration-cache-generated-sources")

    // check to avoid plugin regressions that might affect Gradle's configuration caching
    // https://docs.gradle.org/current/userguide/configuration_cache.html
    fixtureRoot.runBuild("testDebug", "--configuration-cache")
  }

  @Test
  fun rerunRecordOnResourceChange() {
    val fixtureRoot = fixture("rerun-resource-change").clearNestedBuildState()

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()
    snapshotsDir.deleteRecursively()
    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")

    val valuesDir = File(fixtureRoot, "src/main/res/values").registerForDeletionOnExit()
    valuesDir.deleteRecursively()
    val destResourceFile = File(valuesDir, "colors.xml")
    val firstResourceFile = File(fixtureRoot, "src/test/resources/colors1.xml")
    val secondResourceFile = File(fixtureRoot, "src/test/resources/colors2.xml")

    // Original resource
    firstResourceFile.copyTo(destResourceFile, overwrite = false)

    // Take 1
    val firstRunResult = fixtureRoot.runBuild("recordMugshotDebug")

    firstRunResult.assertTaskSucceeded(":testDebugUnitTest")
    assertThat(snapshot.exists()).isTrue()

    val firstRunBytes = snapshot.readBytes()

    // Update resource
    secondResourceFile.copyTo(destResourceFile, overwrite = true)

    // Take 2
    val secondRunResult = fixtureRoot.runBuild("recordMugshotDebug")

    secondRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE
    assertThat(snapshot.exists()).isTrue()

    val secondRunBytes = snapshot.readBytes()

    // should be different colors
    assertThat(firstRunBytes).isNotEqualTo(secondRunBytes)
  }

  @Test
  fun rerunVerifyOnResourceChange() {
    val fixtureRoot = fixture("rerun-resource-change")

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()
    snapshotsDir.deleteRecursively()
    val valuesDir = File(fixtureRoot, "src/main/res/values").registerForDeletionOnExit()
    valuesDir.deleteRecursively()

    val destResourceFile = File(valuesDir, "colors.xml")
    val firstResourceFile = File(fixtureRoot, "src/test/resources/colors1.xml")
    val secondResourceFile = File(fixtureRoot, "src/test/resources/colors2.xml")

    // Original resource
    firstResourceFile.copyTo(destResourceFile, overwrite = false)

    // Setup
    fixtureRoot.runBuild("recordMugshotDebug")

    // Take 1
    val firstRunResult = fixtureRoot.runBuild("verifyMugshotDebug")

    firstRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE

    // Update resource
    secondResourceFile.copyTo(destResourceFile, overwrite = true)

    // Take 2
    val secondRunResult = fixtureRoot.runBuildAndFail("verifyMugshotDebug")

    secondRunResult.assertTaskOutcome(":testDebugUnitTest", TaskOutcome.FAILED) // not UP-TO-DATE
  }

  @Test
  fun rerunOnAssetChange() {
    val fixtureRoot = fixture("rerun-asset-change")

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()
    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")

    val assetsDir = File(fixtureRoot, "src/main/assets").registerForDeletionOnExit()
    val destAssetFile = File(assetsDir, "secret.txt")
    val firstAssetFile = File(fixtureRoot, "src/test/resources/secret1.txt")
    val secondAssetFile = File(fixtureRoot, "src/test/resources/secret2.txt")

    // Original asset
    firstAssetFile.copyTo(destAssetFile, overwrite = false)

    // Take 1
    val firstRunResult = fixtureRoot.runBuild("recordMugshotDebug")

    firstRunResult.assertTaskSucceeded(":testDebugUnitTest")
    assertThat(snapshot.exists()).isTrue()

    val firstRunBytes = snapshot.readBytes()

    // Update asset
    secondAssetFile.copyTo(destAssetFile, overwrite = true)

    // Take 2
    val secondRunResult = fixtureRoot.runBuild("recordMugshotDebug")

    secondRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE
    assertThat(snapshot.exists()).isTrue()

    val secondRunBytes = snapshot.readBytes()

    // should be different
    assertThat(firstRunBytes).isNotEqualTo(secondRunBytes)
  }

  @Test
  fun rerunOnReportDeletion() {
    val fixtureRoot = fixture("rerun-report")
    val reportDir = File(fixtureRoot, "build/reports/mugshot/debug").registerForDeletionOnExit()
    val reportHtml = File(reportDir, "index.html")
    assertThat(reportHtml.exists()).isFalse()

    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    // Take 1
    val firstRunResult = fixtureRoot.runBuild("recordMugshotDebug") { forwardOutput() }

    firstRunResult.assertTaskSucceeded(":testDebugUnitTest")
    assertThat(reportHtml.exists()).isTrue()

    // Remove report
    reportDir.deleteRecursively()

    // Take 2
    val secondRunResult = fixtureRoot.runBuild("recordMugshotDebug")

    secondRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE
    assertThat(reportHtml.exists()).isTrue()
  }

  @Test
  fun rerunOnSnapshotDeletion() {
    val fixtureRoot = fixture("rerun-snapshots")

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()
    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")
    assertThat(snapshot.exists()).isFalse()

    // Take 1
    val firstRunResult = fixtureRoot.runBuild("recordMugshotDebug") { forwardOutput() }

    firstRunResult.assertTaskSucceeded(":testDebugUnitTest")
    assertThat(snapshot.exists()).isTrue()

    // Remove snapshot
    snapshotsDir.deleteRecursively()

    // Take 2
    val secondRunResult = fixtureRoot.runBuild("recordMugshotDebug")

    secondRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE
    assertThat(snapshot.exists()).isTrue()
  }

  @Test
  fun rerunTestsOnPropertyChange() {
    val fixtureRoot = fixture("rerun-property-change")
    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    // Take 1
    val firstRunResult = fixtureRoot.runBuild("testDebugUnitTest") { forwardOutput() }

    firstRunResult.assertTaskSucceeded(":testDebugUnitTest")

    // Take 2
    val secondRunResult = fixtureRoot.runBuild("recordMugshotDebug") { forwardOutput() }

    secondRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE

    // Take 3
    val thirdRunResult = fixtureRoot.runBuild("verifyMugshotDebug") { forwardOutput() }

    thirdRunResult.assertTaskSucceeded(":testDebugUnitTest") // not UP-TO-DATE
  }
}

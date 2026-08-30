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
    val fixtureRoot = File("src/test/projects/prepare-resources-task-caching")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = gradleRunner
      .withArguments("testRelease", "testDebug", "--build-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(firstRun.task(":prepareMugshotDebugResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isNotEqualTo(FROM_CACHE)
    }

    with(firstRun.task(":prepareMugshotReleaseResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isNotEqualTo(FROM_CACHE)
    }

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

    val secondRun = gradleRunner
      .withArguments("testDebug", "--build-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(secondRun.task(":prepareMugshotDebugResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(FROM_CACHE)
    }

    resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("release") }).isFalse()
  }

  @Test
  fun cacheable() {
    val fixtureRoot = File("src/test/projects/cacheable")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = gradleRunner
      .withArguments("testDebug", "--build-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(firstRun.task(":prepareMugshotDebugResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isNotEqualTo(FROM_CACHE)
    }

    buildDir.deleteRecursively()

    val secondRun = gradleRunner
      .withArguments("testDebug", "--build-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(secondRun.task(":prepareMugshotDebugResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(FROM_CACHE)
    }
  }

  @Test
  fun cacheableRelocatable() {
    val fixtureRoot = File("src/test/projects/cacheable")
    fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = gradleRunner
      .withArguments("testDebug", "--build-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(firstRun.task(":prepareMugshotDebugResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isNotEqualTo(FROM_CACHE)
    }
    with(firstRun.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isNotEqualTo(FROM_CACHE)
    }

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

    val secondRun = gradleRunner
      .withArguments("testDebug", "--build-cache", "--stacktrace")
      .runFixture(relocatedRoot) { build() }

    with(secondRun.task(":prepareMugshotDebugResources")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(FROM_CACHE)
    }
    with(secondRun.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(FROM_CACHE)
    }
  }

  @Test
  fun configurationCache() {
    val fixtureRoot = File("src/test/projects/configuration-cache")

    // check to avoid plugin regressions that might affect Gradle's configuration caching
    // https://docs.gradle.org/current/userguide/configuration_cache.html
    gradleRunner
      .withArguments("testDebug", "--configuration-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun configurationCacheWorksWithGeneratedSources() {
    val fixtureRoot = File("src/test/projects/configuration-cache-generated-sources")

    // check to avoid plugin regressions that might affect Gradle's configuration caching
    // https://docs.gradle.org/current/userguide/configuration_cache.html
    gradleRunner
      .withArguments("testDebug", "--configuration-cache", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun rerunRecordOnResourceChange() {
    val fixtureRoot = File("src/test/projects/rerun-resource-change").clearNestedBuildState()

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
    val firstRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(firstRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS)
    }
    assertThat(snapshot.exists()).isTrue()

    val firstRunBytes = snapshot.readBytes()

    // Update resource
    secondResourceFile.copyTo(destResourceFile, overwrite = true)

    // Take 2
    val secondRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(secondRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }
    assertThat(snapshot.exists()).isTrue()

    val secondRunBytes = snapshot.readBytes()

    // should be different colors
    assertThat(firstRunBytes).isNotEqualTo(secondRunBytes)
  }

  @Test
  fun rerunVerifyOnResourceChange() {
    val fixtureRoot = File("src/test/projects/rerun-resource-change")

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
    gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    // Take 1
    val firstRunResult = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(firstRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }

    // Update resource
    secondResourceFile.copyTo(destResourceFile, overwrite = true)

    // Take 2
    val secondRunResult = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    with(secondRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(TaskOutcome.FAILED) // not UP-TO-DATE
    }
  }

  @Test
  fun rerunOnAssetChange() {
    val fixtureRoot = File("src/test/projects/rerun-asset-change")

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()
    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")

    val assetsDir = File(fixtureRoot, "src/main/assets").registerForDeletionOnExit()
    val destAssetFile = File(assetsDir, "secret.txt")
    val firstAssetFile = File(fixtureRoot, "src/test/resources/secret1.txt")
    val secondAssetFile = File(fixtureRoot, "src/test/resources/secret2.txt")

    // Original asset
    firstAssetFile.copyTo(destAssetFile, overwrite = false)

    // Take 1
    val firstRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(firstRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS)
    }
    assertThat(snapshot.exists()).isTrue()

    val firstRunBytes = snapshot.readBytes()

    // Update asset
    secondAssetFile.copyTo(destAssetFile, overwrite = true)

    // Take 2
    val secondRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(secondRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }
    assertThat(snapshot.exists()).isTrue()

    val secondRunBytes = snapshot.readBytes()

    // should be different
    assertThat(firstRunBytes).isNotEqualTo(secondRunBytes)
  }

  @Test
  fun rerunOnReportDeletion() {
    val fixtureRoot = File("src/test/projects/rerun-report")
    val reportDir = File(fixtureRoot, "build/reports/mugshot/debug").registerForDeletionOnExit()
    val reportHtml = File(reportDir, "index.html")
    assertThat(reportHtml.exists()).isFalse()

    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    // Take 1
    val firstRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    with(firstRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS)
    }
    assertThat(reportHtml.exists()).isTrue()

    // Remove report
    reportDir.deleteRecursively()

    // Take 2
    val secondRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(secondRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }
    assertThat(reportHtml.exists()).isTrue()
  }

  @Test
  fun rerunOnSnapshotDeletion() {
    val fixtureRoot = File("src/test/projects/rerun-snapshots")

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()
    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")
    assertThat(snapshot.exists()).isFalse()

    // Take 1
    val firstRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    with(firstRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS)
    }
    assertThat(snapshot.exists()).isTrue()

    // Remove snapshot
    snapshotsDir.deleteRecursively()

    // Take 2
    val secondRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(secondRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }
    assertThat(snapshot.exists()).isTrue()
  }

  @Test
  fun rerunTestsOnPropertyChange() {
    val fixtureRoot = File("src/test/projects/rerun-property-change")
    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    // Take 1
    val firstRunResult = gradleRunner
      .withArguments("testDebugUnitTest", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    with(firstRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS)
    }

    // Take 2
    val secondRunResult = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    with(secondRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }

    // Take 3
    val thirdRunResult = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    with(thirdRunResult.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS) // not UP-TO-DATE
    }
  }
}

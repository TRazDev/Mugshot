package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File

/**
 * Re-running when a resource or asset the snapshots depend on changes.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class RerunOnSourceChangeTest : MugshotPluginTestCase() {
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
}

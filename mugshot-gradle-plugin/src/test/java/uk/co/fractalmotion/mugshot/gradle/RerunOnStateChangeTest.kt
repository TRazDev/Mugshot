package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Re-running when previous output is deleted or a Mugshot property changes.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class RerunOnStateChangeTest : MugshotPluginTestCase() {
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

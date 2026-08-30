package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Per-project configuration: custom directories, flags and Gradle properties.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class PluginOptionsTest : MugshotPluginTestCase() {
  @Test
  fun customBuildDir() {
    val fixtureRoot = fixture("custom-build-dir")
    fixtureRoot.resolve("custom").registerForDeletionOnExit()

    val result = fixtureRoot.runBuild("testDebug") { forwardOutput() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()

    val resourcesFile = File(fixtureRoot, "custom/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()

    val snapshotsDir = File(fixtureRoot, "custom/reports/mugshot/debug/images")
    assertThat(snapshotsDir.exists()).isTrue()
  }

  @Test
  fun customReportDir() {
    val fixtureRoot = fixture("custom-report-dir")
    fixtureRoot.resolve("custom").registerForDeletionOnExit()

    val result = fixtureRoot.runBuild("testDebug") { forwardOutput() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()

    val resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()

    val snapshotsDir = File(fixtureRoot, "custom/our-reports/mugshot/debug/images")
    assertThat(snapshotsDir.exists()).isTrue()
  }

  @Test
  fun invalidChars() {
    val fixtureRoot = fixture("invalid-chars")

    val result = fixtureRoot.runBuildAndFail("testDebug")

    assertThat(result.output).doesNotContain("InvalidCharsTest > goodValues[ADDITION] FAILED")
    assertThat(result.output).doesNotContain("InvalidCharsTest > goodValues[SUBTRACTION] FAILED")
    assertThat(result.output).doesNotContain("InvalidCharsTest > goodValues[MULTIPLICATION] FAILED")
    assertThat(result.output).doesNotContain("InvalidCharsTest > goodValues[DIVISION] FAILED")
    assertThat(result.output).contains("InvalidCharsTest > badSnapshotName FAILED")
    assertThat(result.output).doesNotContain("InvalidCharsTest > badValues[char=+] FAILED")
    assertThat(result.output).doesNotContain("InvalidCharsTest > badValues[char=-] FAILED")
    assertThat(result.output).contains("InvalidCharsTest > badValues[char=*] FAILED")
    assertThat(result.output).contains("InvalidCharsTest > badValues[char=/] FAILED")
  }

  @Test
  fun buildClassAccess() {
    val fixtureRoot = fixture("build-class")

    fixtureRoot.runBuild("testDebug")

    val snapshotsDir = File(fixtureRoot, "custom/reports/mugshot/debug/images")
    assertThat(snapshotsDir.exists()).isFalse()
  }

  @Test
  fun flagDebugLinkedObjectsIsOff() {
    val fixtureRoot = fixture("flag-debug-linked-objects-off")

    val result = fixtureRoot.runBuild("testDebug")

    assertThat(result.output).doesNotContain("Objects still linked from the DelegateManager:")
  }

  @Test
  fun flagDebugLinkedObjectsIsOn() {
    val fixtureRoot = fixture("flag-debug-linked-objects-on")
    // this is only a warning message, so subsequent runs would otherwise be UP-TO-DATE
    fixtureRoot.resolve("build").registerForDeletionOnExit()

    val result = fixtureRoot.runBuild("testDebug")

    assertThat(result.output).contains("Objects still linked from the DelegateManager:")
  }

  @Test
  fun jacoco() {
    val fixtureRoot = fixture("jacoco")

    fixtureRoot.runBuild("testDebug")

    val jacocoExecutionData = File(fixtureRoot, "build/jacoco/testDebugUnitTest.exec")
    assertThat(jacocoExecutionData.exists()).isTrue()
  }

  @Test
  fun configIsUpdatable() = fixture("update-mugshot-config").verifyDebug()

  @Test
  fun maxPercentDifferenceDefaultSet() {
    val fixtureRoot = fixture("max-percent-difference-default-set")
    // this is only a warning message, so subsequent runs would otherwise be UP-TO-DATE
    fixtureRoot.resolve("build").registerForDeletionOnExit()

    val result = fixtureRoot.runBuild("verifyMugshotDebug")

    result.assertTaskSucceeded(":testDebugUnitTest")
  }
}

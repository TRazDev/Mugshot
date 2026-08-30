package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
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
    val fixtureRoot = File("src/test/projects/custom-build-dir")
    fixtureRoot.resolve("custom").registerForDeletionOnExit()

    val result = gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()

    val resourcesFile = File(fixtureRoot, "custom/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()

    val snapshotsDir = File(fixtureRoot, "custom/reports/mugshot/debug/images")
    assertThat(snapshotsDir.exists()).isTrue()
  }

  @Test
  fun customReportDir() {
    val fixtureRoot = File("src/test/projects/custom-report-dir")
    fixtureRoot.resolve("custom").registerForDeletionOnExit()

    val result = gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .forwardOutput()
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()

    val resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()

    val snapshotsDir = File(fixtureRoot, "custom/our-reports/mugshot/debug/images")
    assertThat(snapshotsDir.exists()).isTrue()
  }

  @Test
  fun invalidChars() {
    val fixtureRoot = File("src/test/projects/invalid-chars")

    val result = gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

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
    val fixtureRoot = File("src/test/projects/build-class")

    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    val snapshotsDir = File(fixtureRoot, "custom/reports/mugshot/debug/images")
    assertThat(snapshotsDir.exists()).isFalse()
  }

  @Test
  fun flagDebugLinkedObjectsIsOff() {
    val fixtureRoot = File("src/test/projects/flag-debug-linked-objects-off")

    val result = gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.output).doesNotContain("Objects still linked from the DelegateManager:")
  }

  @Test
  fun flagDebugLinkedObjectsIsOn() {
    val fixtureRoot = File("src/test/projects/flag-debug-linked-objects-on")
    // this is only a warning message, so subsequent runs would otherwise be UP-TO-DATE
    fixtureRoot.resolve("build").registerForDeletionOnExit()

    val result = gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.output).contains("Objects still linked from the DelegateManager:")
  }

  @Test
  fun jacoco() {
    val fixtureRoot = File("src/test/projects/jacoco")

    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    val jacocoExecutionData = File(fixtureRoot, "build/jacoco/testDebugUnitTest.exec")
    assertThat(jacocoExecutionData.exists()).isTrue()
  }

  @Test
  fun configIsUpdatable() {
    val fixtureRoot = File("src/test/projects/update-mugshot-config")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun maxPercentDifferenceDefaultSet() {
    val fixtureRoot = File("src/test/projects/max-percent-difference-default-set")
    // this is only a warning message, so subsequent runs would otherwise be UP-TO-DATE
    fixtureRoot.resolve("build").registerForDeletionOnExit()

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    with(result.task(":testDebugUnitTest")) {
      assertThat(this).isNotNull()
      assertThat(this!!.outcome).isEqualTo(SUCCESS)
    }
  }
}

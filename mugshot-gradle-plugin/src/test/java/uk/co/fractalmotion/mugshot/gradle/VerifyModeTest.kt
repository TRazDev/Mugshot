package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Test
import uk.co.fractalmotion.mugshot.gradle.ImageSubject.Companion.assertThat
import java.io.File

/**
 * `verifyMugshot*` on the passing paths, plus the HTML report.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class VerifyModeTest : MugshotPluginTestCase() {
  @Test
  fun verifySuccess() {
    val fixtureRoot = File("src/test/projects/verify-mode-success")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()
  }

  @Test
  fun verifyAllVariants() {
    val fixtureRoot = File("src/test/projects/verify-mode-success")

    val result = gradleRunner
      .withArguments("verifyMugshot", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":verifyMugshotDebug")).isNotNull()
    assertThat(result.task(":verifyMugshotRelease")).isNotNull()
  }

  @Test
  fun verifyDeletesFailures() {
    val fixtureRoot = File("src/test/projects/verify-mode-success")
    val failureDir = File(fixtureRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()
    failureDir.mkdirs()
    val stale = File(failureDir, "stale.txt")
    stale.writeText("stale")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":testDebugUnitTest")?.outcome).isEqualTo(SUCCESS)
    assertThat(stale.exists()).isFalse()
  }

  @Test
  fun recordPreservesFailures() {
    val fixtureRoot = File("src/test/projects/verify-mode-success")
    val failureDir = File(fixtureRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()
    failureDir.mkdirs()
    val stale = File(failureDir, "stale.txt")
    stale.writeText("stale")

    val result = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":testDebugUnitTest")?.outcome).isEqualTo(SUCCESS)
    assertThat(stale.exists()).isTrue()
  }

  @Test
  fun verifySuccessMultiModule() {
    val fixtureRoot = File("src/test/projects/verify-mode-success-multiple-modules")

    val result = gradleRunner
      .withArguments("module:verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":module:testDebugUnitTest")).isNotNull()
  }

  @Test
  fun snapshotReport() {
    val fixtureRoot = File("src/test/projects/report-snapshots")
    val testReportDir = File(fixtureRoot, "build/reports/tests/testDebugUnitTest/classes")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    val testTask = result.task(":testDebugUnitTest")
    assertThat(testTask).isNotNull()
    assertThat(testTask!!.outcome).isEqualTo(TaskOutcome.FAILED)

    val simpleTestHtmlFile = File(testReportDir, "uk.co.fractalmotion.mugshot.plugin.test.SimpleTest.html")
    var htmlText = simpleTestHtmlFile.readText()
    assertThat(htmlText).contains("<img")
    assertThat(htmlText).contains("delta-uk.co.fractalmotion.mugshot.plugin.test_SimpleTest_compose.webp")
    assertThat(htmlText).contains("Failed tests")
    assertThat(htmlText).contains("Tests")
    assertThat(htmlText).contains("Standard output")
    assertThat(htmlText).contains("Standard error")

    val testParamInjectorTestHtmlFile =
      File(testReportDir, "uk.co.fractalmotion.mugshot.plugin.test.TestParameterInjectorTest.html")
    htmlText = testParamInjectorTestHtmlFile.readText()
    assertThat(htmlText).contains("<img")
    assertThat(htmlText).contains("delta-uk.co.fractalmotion.mugshot.plugin.test_TestParameterInjectorTest_compose[darkMode=false,fontScale=1.0].webp")
    assertThat(htmlText).contains("Failed tests")
    assertThat(htmlText).contains("Tests")
    assertThat(htmlText).contains("Standard output")
    assertThat(htmlText).contains("Standard error")
  }

  @Test
  fun verifyMissingGolden() {
    val fixtureRoot = File("src/test/projects/verify-missing-golden")

    val fileName = "uk.co.fractalmotion.mugshot.plugin.test_VerifyTest_verify.webp"
    val snapshot = File(fixtureRoot, "src/test/snapshots/images/$fileName")
    assertThat(snapshot.exists()).isFalse()

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace", "--info")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()

    val failureDir = File(fixtureRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()

    val golden = File(failureDir, fileName)
    assertThat(golden.exists()).isTrue()

    val delta = File(failureDir, "delta-$fileName")
    assertThat(delta.exists()).isTrue()

    val expectedDelta = File(fixtureRoot, "src/test/resources/expected_delta.webp")
    assertThat(delta).isSimilarTo(expectedDelta).withDefaultThreshold()
  }
}

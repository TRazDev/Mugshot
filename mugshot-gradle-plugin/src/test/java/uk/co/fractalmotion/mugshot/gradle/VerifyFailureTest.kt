package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uk.co.fractalmotion.mugshot.gradle.ImageSubject.Companion.assertThat
import java.io.File

/**
 * `verifyMugshot*` on the failing paths, including the delta images it emits.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class VerifyFailureTest : MugshotPluginTestCase() {
  @Test
  fun verifyFailure() {
    val fixtureRoot = File("src/test/projects/verify-mode-failure")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()

    val failureDir = File(fixtureRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()
    val delta = File(failureDir, "delta-uk.co.fractalmotion.mugshot.plugin.test_VerifyTest_verify.webp")
    assertThat(delta.exists()).isTrue()

    val goldenImage = File(fixtureRoot, "src/test/resources/expected_delta.webp")
    assertThat(delta).isSimilarTo(goldenImage).withDefaultThreshold()
  }

  @Test
  fun verifySimilar() {
    val fixtureRoot = File("src/test/projects/verify-similar")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()

    val failureDir = File(fixtureRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()
    val delta = File(failureDir, "delta-uk.co.fractalmotion.mugshot.plugin.test_VerifyTest_verify.webp")
    assertThat(delta.exists()).isTrue()

    val goldenImage = File(fixtureRoot, "src/test/resources/expected_delta.webp")
    assertThat(delta).isSimilarTo(goldenImage).withDefaultThreshold()
  }

  @Test
  fun verifySize() {
    val fixtureRoot = File("src/test/projects/verify-size")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()

    val failureDir = File(fixtureRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()
    val delta = File(failureDir, "delta-uk.co.fractalmotion.mugshot.plugin.test_VerifyTest_verify.webp")
    assertThat(delta.exists()).isTrue()

    val goldenImage = File(fixtureRoot, "src/test/resources/expected_delta.webp")
    assertThat(delta).isSimilarTo(goldenImage).withDefaultThreshold()
  }

  @Test
  fun verifyFailureMultiModule() {
    val fixtureRoot = File("src/test/projects/verify-mode-failure-multiple-modules")
    val moduleRoot = File(fixtureRoot, "module")

    val result = gradleRunner
      .withArguments("module:verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":module:testDebugUnitTest")).isNotNull()

    val failureDir = File(moduleRoot, "build/mugshot/failures/debug").registerForDeletionOnExit()
    val delta = File(failureDir, "delta-uk.co.fractalmotion.mugshot.plugin.test_VerifyTest_verify.webp")
    assertThat(delta.exists()).isTrue()

    val goldenImage = File(moduleRoot, "src/test/resources/expected_delta.webp")
    assertThat(delta).isSimilarTo(goldenImage).withDefaultThreshold()
  }
}

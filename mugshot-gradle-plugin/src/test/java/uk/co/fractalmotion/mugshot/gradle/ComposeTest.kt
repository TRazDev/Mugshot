package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Rendering and lifecycle behaviour for Jetpack Compose.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class ComposeTest : MugshotPluginTestCase() {
  @Test
  fun compose() {
    val fixtureRoot = File("src/test/projects/compose")
    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun composeLeaks() {
    val fixtureRoot = File("src/test/projects/compose-leaks")

    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun composeRecomposition() {
    val fixtureRoot = File("src/test/projects/compose-recomposition")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun composeWear() {
    val fixtureRoot = File("src/test/projects/compose-wear")
    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun composeViewTreeLifecycle() {
    val fixtureRoot = File("src/test/projects/compose-lifecycle-owner")
    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    val snapshotsDir = File(fixtureRoot, "build/reports/mugshot/debug/images")
    val snapshots = snapshotsDir.listFiles()
    assertThat(snapshots!!).hasLength(1)
  }

  @Test
  fun composeLaunchedEffectExceptionPropagates() {
    val fixtureRoot = File("src/test/projects/compose-launched-effect-exception")

    val result = gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()
    assertThat(result.output).contains("LaunchedEffectExceptionTest > launchedEffectExceptionPropagates FAILED")
    assertThat(result.output).contains("java.lang.IllegalStateException: Exception thrown in LaunchedEffect")
  }
}

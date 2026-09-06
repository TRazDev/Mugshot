package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Rendering and lifecycle behaviour for Jetpack Compose.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class ComposeTest : MugshotPluginTestCase() {
  @Test
  fun compose() {
    val fixtureRoot = fixture("compose")
    fixtureRoot.runBuild("verifyMugshotDebug")
  }

  @Test
  fun composeLeaks() = fixture("compose-leaks").buildSucceeds("testDebug")

  @Test
  fun composeRecomposition() = fixture("compose-recomposition").verifyDebug()

  @Test
  fun composeWear() {
    val fixtureRoot = fixture("compose-wear")
    fixtureRoot.runBuild("verifyMugshotDebug")
  }

  @Test
  fun composeViewTreeLifecycle() = fixture("compose-lifecycle-owner").buildSucceeds("testDebug")

  @Test
  fun composeLaunchedEffectExceptionPropagates() {
    val fixtureRoot = fixture("compose-launched-effect-exception")

    val result = fixtureRoot.runBuildAndFail("testDebug")

    assertThat(result.task(":testDebugUnitTest")).isNotNull()
    assertThat(result.output).contains("LaunchedEffectExceptionTest > launchedEffectExceptionPropagates FAILED")
    assertThat(result.output).contains("java.lang.IllegalStateException: Exception thrown in LaunchedEffect")
  }
}

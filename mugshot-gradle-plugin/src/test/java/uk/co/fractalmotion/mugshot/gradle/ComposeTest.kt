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
  fun composeViewTreeLifecycle() {
    val fixtureRoot = fixture("compose-lifecycle-owner")
    fixtureRoot.runBuild("testDebug")

    val snapshotsDir = File(fixtureRoot, "build/reports/mugshot/debug/images")
    val snapshots = snapshotsDir.listFiles()
    assertThat(snapshots!!).hasLength(1)
  }

  @Test
  fun composeLaunchedEffectExceptionPropagates() {
    val fixtureRoot = fixture("compose-launched-effect-exception")

    val result = fixtureRoot.runBuildAndFail("testDebug")

    assertThat(result.task(":testDebugUnitTest")).isNotNull()
    assertThat(result.output).contains("LaunchedEffectExceptionTest > launchedEffectExceptionPropagates FAILED")
    assertThat(result.output).contains("java.lang.IllegalStateException: Exception thrown in LaunchedEffect")
  }
}

package uk.co.fractalmotion.mugshot.gradle

import org.junit.Test
import java.io.File

/**
 * Rendering under device configuration: locale, layout direction, night mode, orientation.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class DeviceConfigRenderingTest : MugshotPluginTestCase() {
  @Test
  fun localeQualifier() = fixture("locale-qualifier").verifyDebug()

  @Test
  fun layoutDirection() = fixture("layout-direction").verifyDebug()

  @Test
  fun nightMode() = fixture("night-mode").verifyDebug()

  @Test
  fun screenOrientation() = fixture("verify-orientation").verifyDebug()

  @Test
  fun screenRound() = fixture("verify-screen-round").verifyDebug()

  @Test
  fun scaledVersusFullDeviceResolution() = fixture("device-resolution").verifyDebug()

  @Test
  fun verifyRenderingModes() = fixture("verify-rendering-modes").verifyDebug()
}

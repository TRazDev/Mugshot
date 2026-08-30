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
  fun localeQualifier() {
    val fixtureRoot = File("src/test/projects/locale-qualifier")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun layoutDirection() {
    val fixtureRoot = File("src/test/projects/layout-direction")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun nightMode() {
    val fixtureRoot = File("src/test/projects/night-mode")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun screenOrientation() {
    val fixtureRoot = File("src/test/projects/verify-orientation")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun screenRound() {
    val fixtureRoot = File("src/test/projects/verify-screen-round")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun scaledVersusFullDeviceResolution() {
    val fixtureRoot = File("src/test/projects/device-resolution")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun verifyRenderingModes() {
    val fixtureRoot = File("src/test/projects/verify-rendering-modes")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }
}

package uk.co.fractalmotion.mugshot.sample.screen

import com.android.resources.ScreenOrientation
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.catalog.SampleDevice
import uk.co.fractalmotion.mugshot.sample.catalog.Screen
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

/** The knobs that are turned on the rule itself rather than through `DeviceConfig`. */
class RuntimeConfigSnapshotTest {
  @get:Rule val mugshot = Mugshot(deviceConfig = SampleDevice.COMPACT.config)

  /**
   * Two orientations in one test.
   *
   * `unsafeUpdateConfig` swaps the device between snapshots without a second rule, which is how you
   * capture a before and after — a rotation, a theme change — in a single test. It is "unsafe"
   * because it tears down and rebuilds the render session underneath you, so treat it as a
   * checkpoint rather than something to call in a loop.
   */
  @Test
  fun rotate() {
    mugshot.snapshot(name = "portrait") { MugshotTheme { Screen.SMART_HOME.content() } }

    mugshot.unsafeUpdateConfig(
      deviceConfig = SampleDevice.COMPACT.config.copy(
        orientation = ScreenOrientation.LANDSCAPE
      )
    )

    mugshot.snapshot(name = "landscape") { MugshotTheme { Screen.SMART_HOME.content() } }
  }
}

/**
 * `showSystemUi` draws the framework status and navigation bars around the content.
 *
 * It needs a theme that actually has those bars, so the rule swaps off Mugshot's default fullscreen
 * theme. Separate class because both settings are constructor arguments.
 */
class SystemUiSnapshotTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = SampleDevice.COMPACT.config,
    theme = "android:Theme.Material.Light",
    showSystemUi = true
  )

  @Test
  fun withSystemBars() {
    mugshot.snapshot { MugshotTheme { Screen.PROFILE.content() } }
  }
}

/**
 * `useDeviceResolution` records at the device's real pixel size instead of the scaled thumbnail
 * Mugshot writes by default.
 *
 * Run on the smallest device in the matrix on purpose: the flag multiplies the golden's dimensions,
 * and these images are committed to the repository.
 */
class DeviceResolutionSnapshotTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.NEXUS_4,
    useDeviceResolution = true
  )

  @Test
  fun fullResolution() {
    mugshot.snapshot { MugshotTheme { Screen.WORKOUT_DETAIL.content() } }
  }
}

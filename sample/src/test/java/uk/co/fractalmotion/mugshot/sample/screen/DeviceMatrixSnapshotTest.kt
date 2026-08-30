package uk.co.fractalmotion.mugshot.sample.screen

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.catalog.SampleDevice
import uk.co.fractalmotion.mugshot.sample.catalog.Screen
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

/**
 * One screen across the shapes Android actually ships in.
 *
 * `DeviceConfig` carries 37 presets; these four cover the interesting failure modes — a phone, a
 * tablet's extra width, a fold's aspect ratio, and landscape, where a vertically scrolling screen
 * has to cope with a fraction of the height.
 */
@RunWith(TestParameterInjector::class)
class DeviceMatrixSnapshotTest(
  @TestParameter(value = ["PHONE", "TABLET", "FOLD", "LANDSCAPE"])
  private val device: SampleDevice
) {
  @get:Rule val mugshot = Mugshot(deviceConfig = device.config)

  @Test
  fun health() {
    mugshot.snapshot { MugshotTheme { Screen.HEALTH.content() } }
  }
}

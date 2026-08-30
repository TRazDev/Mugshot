package uk.co.fractalmotion.mugshot.sample.screen

import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCompactStat
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

/**
 * A round device.
 *
 * The wear presets set `screenRound = ROUND`, and under `RenderingMode.NORMAL` Mugshot clips the
 * rendered frame to a circle — so the golden has genuinely round corners rather than a square image
 * of a round watch face. Rendering a phone screen here would prove nothing, so this uses a
 * composable actually designed for the shape.
 */
class WearSnapshotTest {
  @get:Rule val mugshot = Mugshot(deviceConfig = DeviceConfig.WEAR_OS_SMALL_ROUND)

  @Test
  fun watchFace() {
    mugshot.snapshot {
      MugshotTheme {
        MugshotCompactStat(progress = 0.72f, label = "Move", value = "72%")
      }
    }
  }
}

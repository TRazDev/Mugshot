package uk.co.fractalmotion.mugshot.plugin.test

import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import org.junit.Rule
import org.junit.Test

class ComposeTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.WEAR_OS_SMALL_ROUND,
    theme = "android:ThemeOverlay.Material.Dark"
  )

  @Test
  fun compose() {
    mugshot.snapshot {
      HelloMugshot()
    }
  }
}

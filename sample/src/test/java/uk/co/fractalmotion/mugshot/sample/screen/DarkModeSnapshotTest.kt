package uk.co.fractalmotion.mugshot.sample.screen

import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.catalog.Screen
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

/**
 * Every screen in dark mode.
 *
 * Note what is *not* here: nothing tells the theme to go dark. `nightMode = NIGHT` puts layoutlib
 * into night configuration, `MugshotTheme` reads that through `isSystemInDarkTheme()`, and the
 * `-night` string qualifier resolves at the same time — the "Dark theme" chip on the profile screen
 * is proof of the second half.
 *
 * `maxPercentDifference` is left at its default here; it is a verification threshold rather than
 * anything a golden can show, and raising it would only hide real regressions.
 */
@RunWith(TestParameterInjector::class)
class DarkModeSnapshotTest(@TestParameter private val screen: Screen) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.PIXEL_6.copy(nightMode = NightMode.NIGHT)
  )

  @Test
  fun dark() {
    mugshot.snapshot { MugshotTheme { screen.content() } }
  }
}

package uk.co.fractalmotion.mugshot.sample.screen

import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.feature.profile.ProfileFixtures
import uk.co.fractalmotion.mugshot.sample.feature.profile.ProfileScreen
import uk.co.fractalmotion.mugshot.sample.feature.storefront.StorefrontFixtures
import uk.co.fractalmotion.mugshot.sample.feature.storefront.StorefrontScreen

/**
 * The plainest thing Mugshot can do: a rule, a composable, a golden.
 *
 * Every other test in this package varies one axis of the library on top of this. If you are
 * copying one file out of the sample to start your own suite, copy this one.
 */
class ScreenSnapshotTest {
  @get:Rule val mugshot = Mugshot(deviceConfig = DeviceConfig.PIXEL_6)

  @Test
  fun profile() {
    mugshot.snapshot {
      MugshotTheme { ProfileScreen(state = ProfileFixtures.sample) }
    }
  }

  @Test
  fun storefront() {
    mugshot.snapshot {
      MugshotTheme { StorefrontScreen(state = StorefrontFixtures.populated) }
    }
  }
}

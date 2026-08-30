package uk.co.fractalmotion.mugshot.sample.screen

import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.catalog.SampleDevice
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.feature.profile.ProfileFixtures
import uk.co.fractalmotion.mugshot.sample.feature.profile.ProfileScreen

/**
 * What the three rendering modes do to a screen taller than its device.
 *
 * The profile screen scrolls, so the difference is easy to see:
 *  - `NORMAL` fills the device and clips whatever does not fit.
 *  - `V_SCROLL` expands the image to the full scrollable height, so the golden shows the whole
 *    screen at once. This is usually what you want for a long form.
 *  - `SHRINK` wraps to the content, which for a screen that fills the viewport looks much like
 *    `NORMAL`, but for a dialog or a component is the difference between a specimen and a page.
 */
@RunWith(TestParameterInjector::class)
class RenderingModeSnapshotTest(@TestParameter private val mode: Mode) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = SampleDevice.COMPACT.config,
    renderingMode = mode.renderingMode
  )

  @Test
  fun profile() {
    mugshot.snapshot {
      MugshotTheme {
        // V_SCROLL measures with unbounded height so it can draw the whole screen, which is exactly
        // what Modifier.verticalScroll refuses to be measured with. Let the renderer do the
        // scrolling instead of the screen.
        ProfileScreen(state = ProfileFixtures.sample, scrollable = mode.allowsContentScrolling)
      }
    }
  }

  enum class Mode(val renderingMode: RenderingMode, val allowsContentScrolling: Boolean) {
    NORMAL(RenderingMode.NORMAL, allowsContentScrolling = true),
    V_SCROLL(RenderingMode.V_SCROLL, allowsContentScrolling = false),
    SHRINK(RenderingMode.SHRINK, allowsContentScrolling = true)
  }
}

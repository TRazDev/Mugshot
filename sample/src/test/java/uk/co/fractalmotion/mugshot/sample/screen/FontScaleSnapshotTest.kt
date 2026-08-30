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
 * Screens at the text sizes people actually use.
 *
 * [TextSize.HUGE] is the interesting one: 2x is where fixed heights clip, single-line labels
 * collide and a row of stats stops fitting. Two screens is enough to catch that, so the matrix is
 * curated rather than run over all seven.
 */
@RunWith(TestParameterInjector::class)
class FontScaleSnapshotTest(
  @TestParameter(value = ["PROFILE", "HEALTH"]) private val screen: Screen,
  @TestParameter private val textSize: TextSize
) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = SampleDevice.COMPACT.config.copy(fontScale = textSize.scale)
  )

  @Test
  fun scaled() {
    mugshot.snapshot { MugshotTheme { screen.content() } }
  }

  enum class TextSize(val scale: Float) {
    LARGE(scale = 1.3f),
    HUGE(scale = 2.0f)
  }
}

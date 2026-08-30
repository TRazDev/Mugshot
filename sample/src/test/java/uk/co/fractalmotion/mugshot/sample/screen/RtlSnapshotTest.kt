package uk.co.fractalmotion.mugshot.sample.screen

import com.android.resources.LayoutDirection
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
 * Right to left layout.
 *
 * Separate from [LocalizationSnapshotTest] because `supportsRtl` is a constructor argument on the
 * rule rather than part of `DeviceConfig`, so it cannot be varied per test method the way a locale
 * can. Both halves are needed: `layoutDirection = RTL` mirrors the layout and `supportsRtl = true`
 * makes layoutlib honour it.
 *
 * These render English strings on purpose. Mirroring is a property of the layout, not of the text,
 * and seeing familiar words in a mirrored layout makes it obvious which paddings and alignments
 * were hardcoded to the left.
 */
@RunWith(TestParameterInjector::class)
class RtlSnapshotTest(
  @TestParameter(value = ["PROFILE", "STOREFRONT"]) private val screen: Screen
) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = SampleDevice.COMPACT.config.copy(layoutDirection = LayoutDirection.RTL),
    supportsRtl = true
  )

  @Test
  fun mirrored() {
    mugshot.snapshot { MugshotTheme { screen.content() } }
  }
}

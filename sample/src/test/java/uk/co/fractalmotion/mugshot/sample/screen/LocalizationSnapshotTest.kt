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
 * Real and pseudo locales.
 *
 * Pinned to the profile screen because that is the screen with an authored Arabic translation; the
 * other six would fall back to English and produce goldens that look broken rather than
 * instructive. Arabic earns its place twice over — it uses all six CLDR plural categories, and the
 * profile screen's "12 workouts this month" line goes through `pluralStringResource`.
 *
 * The two pseudolocales need no translation at all. `en-rXA` pads and accents every string, which
 * is how you find text that will not survive a longer language; `ar-rXB` mirrors it. Both are
 * generated in process, and both only transform string *resources* — a hardcoded Kotlin literal
 * would pass straight through, which is why the screens keep every visible string in `strings.xml`.
 */
@RunWith(TestParameterInjector::class)
class LocalizationSnapshotTest(@TestParameter private val locale: SampleLocale) {
  @get:Rule
  val mugshot = Mugshot(deviceConfig = SampleDevice.COMPACT.config.copy(locale = locale.tag))

  @Test
  fun localized() {
    mugshot.snapshot { MugshotTheme { Screen.PROFILE.content() } }
  }

  enum class SampleLocale(val tag: String) {
    ARABIC("ar"),
    PSEUDO_ACCENT("en-rXA"),
    PSEUDO_BIDI("ar-rXB")
  }
}

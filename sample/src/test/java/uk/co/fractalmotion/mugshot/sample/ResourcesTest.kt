package uk.co.fractalmotion.mugshot.sample

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot

@RunWith(TestParameterInjector::class)
class ResourcesTest(
  @TestParameter locale: Locale
) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.PIXEL_5.copy(locale = locale.tag)
  )

  @Test
  fun legacy() {
    mugshot.snapshot(ResourcesDemoView(mugshot.context))
  }

  @Test
  fun compose() {
    mugshot.snapshot { ResourcesDemo() }
  }

  enum class Locale(val tag: String?) {
    Default(null),
    Arabic("ar"),
    Accent("en-rXA"),
    Bidi("ar-rXB")
  }
}

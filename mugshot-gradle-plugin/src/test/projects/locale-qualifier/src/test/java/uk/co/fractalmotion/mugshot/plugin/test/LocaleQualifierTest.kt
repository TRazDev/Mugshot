package uk.co.fractalmotion.mugshot.plugin.test

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot

@RunWith(TestParameterInjector::class)
class LocaleQualifierTest(
  @TestParameter locale: Locale
) {
  enum class Locale(val tag: String?) {
    Default(null),
    GB("en-rGB")
  }

  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.NEXUS_5.copy(
      locale = locale.tag
    )
  )

  @Test
  fun locale() {
    mugshot.snapshot(mugshot.inflate(R.layout.title_color))
  }
}

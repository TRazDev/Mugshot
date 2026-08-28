package uk.co.fractalmotion.mugshot.plugin.test

import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot

@RunWith(TestParameterInjector::class)
class NightModeTest(
  @TestParameter nightMode: NightMode
) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.NEXUS_5.copy(nightMode = nightMode)
  )

  @Test
  fun xml() {
    mugshot.snapshot(mugshot.inflate(R.layout.layout))
  }

  @Test
  fun compose() {
    mugshot.snapshot { LightDark() }
  }
}

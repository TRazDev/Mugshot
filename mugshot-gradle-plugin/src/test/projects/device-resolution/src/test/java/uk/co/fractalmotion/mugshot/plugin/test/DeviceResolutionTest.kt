package uk.co.fractalmotion.mugshot.plugin.test

import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DeviceResolutionTest(
  @TestParameter useDeviceResolution: Boolean
) {

  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.NEXUS_5,
    useDeviceResolution = useDeviceResolution
  )

  @Test
  fun deviceResolution() {
    mugshot.snapshot(mugshot.inflate(R.layout.launch))
  }
}

package uk.co.fractalmotion.mugshot.plugin.test

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot

/** The device's own resolution is a downscale of 1; the default renders a third of it. */
@RunWith(TestParameterInjector::class)
class DeviceResolutionTest(
  @TestParameter("1", "3") private val downscale: Int
) {

  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.NEXUS_5,
    downscale = downscale.toFloat()
  )

  @Test
  fun deviceResolution() {
    mugshot.snapshot(mugshot.inflate(R.layout.launch))
  }
}

package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot

@RunWith(TestParameterInjector::class)
class TestParameterInjectorTest(
  @TestParameter val darkMode: Boolean,
  @TestParameter("1", "2") val fontScale: Float
) {
  @get:Rule
  val mugshot = Mugshot(
    maxPercentDifference = 0.0,
    deviceConfig = DeviceConfig.PIXEL.copy(fontScale = fontScale)
  )

  @Test
  fun compose() {
    mugshot.snapshot {
      Box(
        Modifier.background(
          if (darkMode) Color(0xFF66ffc7) else Color(0xFF1EB980)
        )
      ) {
        Text("Hello, Mugshot")
      }
    }
  }
}

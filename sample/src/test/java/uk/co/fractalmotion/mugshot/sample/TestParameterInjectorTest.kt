package uk.co.fractalmotion.mugshot.sample

import android.widget.LinearLayout
import com.android.resources.ScreenOrientation.LANDSCAPE
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.databinding.KeypadBinding

@RunWith(TestParameterInjector::class)
class TestParameterInjectorTest(
  @TestParameter config: Config
) {
  enum class Config(
    val deviceConfig: DeviceConfig
  ) {
    NEXUS_4(deviceConfig = DeviceConfig.NEXUS_4),
    NEXUS_5(deviceConfig = DeviceConfig.NEXUS_5),
    NEXUS_5_LAND(deviceConfig = DeviceConfig.NEXUS_5.copy(orientation = LANDSCAPE))
  }

  enum class Theme(val themeName: String) {
    LIGHT("android:Theme.Material.Light"),
    LIGHT_NO_ACTION_BAR("android:Theme.Material.Light.NoActionBar")
  }

  object AmountProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<String> = listOf("\$1.00", "\$2.00", "\$5.00", "\$10.00")
  }

  @get:Rule
  val mugshot = Mugshot(deviceConfig = config.deviceConfig)

  @Test
  fun simple() {
    val launch = mugshot.inflate<LinearLayout>(R.layout.launch)
    mugshot.snapshot(launch)
  }

  @Test
  fun simpleWithTheme(@TestParameter theme: Theme) {
    mugshot.unsafeUpdateConfig(theme = theme.themeName)
    val launch = mugshot.inflate<LinearLayout>(R.layout.launch)
    mugshot.snapshot(launch)
  }

  @Test
  fun amountProviderTest(@TestParameter(valuesProvider = AmountProvider::class) amount: String) {
    val binding = KeypadBinding.inflate(mugshot.layoutInflater)
    binding.amount.text = amount
    mugshot.snapshot(binding.root)
  }
}

package uk.co.fractalmotion.mugshot.plugin.test

import android.view.Gravity
import android.widget.TextView
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.TestName

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JupiterWithoutExtensionTest {

  lateinit var mugshot: Mugshot

  @BeforeEach
  fun setup(testInfo: TestInfo) {
    val name = TestName(
      packageName = testInfo.testClass.get().`package`?.name.orEmpty(),
      className = testInfo.testClass.get().simpleName,
      methodName = testInfo.testMethod.get().name
    )
    mugshot = Mugshot()
    mugshot.setup(testName = name)
  }

  @AfterEach
  fun tearDown() {
    mugshot.teardown()
  }

  @ParameterizedTest(name = "Jupiter param test: {0}")
  @ValueSource(strings = ["1", "2"])
  fun `verify parametrized snapshot`(param: String) {
    val textView = mugshot.inflate<TextView>(android.R.layout.simple_list_item_1)
    textView.apply {
      text = "Jupiter test no extension $param"
      textSize = 24f
      gravity = Gravity.CENTER
    }

    mugshot.snapshot(view = textView, name = param)
  }
}

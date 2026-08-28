package uk.co.fractalmotion.mugshot.plugin.test

import android.view.Gravity
import android.widget.TextView
import uk.co.fractalmotion.mugshot.Mugshot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import runner.MugshotExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MugshotJupiterTest {

  @RegisterExtension
  val mugshotExtension = MugshotExtension(Mugshot())

  @Test
  fun `verify mugshot jupiter snapshot`() {
    val textView = mugshotExtension.api.inflate<TextView>(android.R.layout.simple_list_item_1)
    textView.apply {
      text = "Mugshot Jupiter test"
      textSize = 24f
      gravity = Gravity.CENTER
    }

    mugshotExtension.api.snapshot(textView)
  }
}

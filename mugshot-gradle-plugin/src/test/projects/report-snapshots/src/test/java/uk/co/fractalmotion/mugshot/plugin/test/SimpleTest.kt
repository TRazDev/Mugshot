package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.material.Text
import uk.co.fractalmotion.mugshot.Mugshot
import org.junit.Rule
import org.junit.Test

class SimpleTest {
  @get:Rule
  val mugshot = Mugshot(maxPercentDifference = 0.0)

  @Test
  fun compose() {
    mugshot.snapshot {
      Text("Hello Mugshot!")
    }
  }
}

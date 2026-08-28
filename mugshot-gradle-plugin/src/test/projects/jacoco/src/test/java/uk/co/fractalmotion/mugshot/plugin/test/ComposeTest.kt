package uk.co.fractalmotion.mugshot.plugin.test

import uk.co.fractalmotion.mugshot.Mugshot
import org.junit.Rule
import org.junit.Test

class ComposeTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun compose() {
    mugshot.snapshot {
      HelloMugshot()
    }
  }
}

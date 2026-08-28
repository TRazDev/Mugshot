package uk.co.fractalmotion.mugshot.plugin.test

import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

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

package uk.co.fractalmotion.mugshot.plugin.test

import android.view.View
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class RecordTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun record() {
    mugshot.snapshot(View(mugshot.context))
  }
}

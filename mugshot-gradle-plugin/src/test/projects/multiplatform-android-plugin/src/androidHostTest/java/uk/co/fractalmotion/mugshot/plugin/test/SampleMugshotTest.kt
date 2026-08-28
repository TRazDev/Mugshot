package uk.co.fractalmotion.mugshot.plugin.test

import android.widget.ImageView
import android.widget.TextView
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.plugin.test.R

class SampleMugshotTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun text() {
    mugshot.snapshot(
      TextView(mugshot.context).apply {
        text = "Hello Mugshot from Android Multiplatform Library!"
        textSize = 18f
      }
    )
  }

  @Test
  fun image() {
    mugshot.snapshot(
      ImageView(mugshot.context).apply {
        setImageResource(R.drawable.camera)
      }
    )
  }
}

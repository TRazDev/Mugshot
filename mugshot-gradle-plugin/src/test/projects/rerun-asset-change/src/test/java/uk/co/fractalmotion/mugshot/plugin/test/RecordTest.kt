package uk.co.fractalmotion.mugshot.plugin.test

import android.widget.FrameLayout
import android.widget.TextView
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class RecordTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun record() {
    val contents =
      mugshot.context.assets.open("secret.txt").bufferedReader().use { it.readText() }
    val root = mugshot.inflate<FrameLayout>(R.layout.root)
    val label = root.findViewById<TextView>(R.id.secret)!!
    label.text = contents
    mugshot.snapshot(root)
  }
}

package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class LaunchedEffectExceptionTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun launchedEffectExceptionPropagates() {
    mugshot.snapshot {
      LaunchedEffect(Unit) {
        error("Exception thrown in LaunchedEffect")
      }
      Text("Hello")
    }
  }
}

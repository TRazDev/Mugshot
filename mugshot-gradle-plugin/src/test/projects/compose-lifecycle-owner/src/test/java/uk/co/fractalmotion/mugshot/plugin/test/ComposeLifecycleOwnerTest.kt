package uk.co.fractalmotion.mugshot.plugin.test

import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class ComposeLifecycleOwnerTest {
  @get:Rule
  val mugshot = Mugshot(
    renderExtensions = setOf()
  )

  @Test
  fun lifecycleOwnerAvailableWithRendererExtension() {
    mugshot.snapshot {
      HelloMugshot()
    }
  }
}

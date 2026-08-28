package uk.co.fractalmotion.mugshot.plugin.test

import uk.co.fractalmotion.mugshot.Mugshot
import org.junit.Rule
import org.junit.Test

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

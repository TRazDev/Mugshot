package app.cash.paparazzi.plugin.test

import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class ComposeLifecycleOwnerTest {
  @get:Rule
  val paparazzi = Paparazzi(
    renderExtensions = setOf()
  )

  @Test
  fun lifecycleOwnerAvailableWithRendererExtension() {
    paparazzi.snapshot {
      HelloPaparazzi()
    }
  }
}

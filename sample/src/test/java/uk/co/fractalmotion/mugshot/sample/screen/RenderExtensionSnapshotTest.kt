package uk.co.fractalmotion.mugshot.sample.screen

import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.catalog.SampleDevice
import uk.co.fractalmotion.mugshot.sample.catalog.Screen
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

/**
 * A custom [uk.co.fractalmotion.mugshot.RenderExtension].
 *
 * Extensions are passed to the rule as a set and applied to every snapshot it takes, so this is the
 * place to put cross cutting decoration rather than editing each screen.
 */
class RenderExtensionSnapshotTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = SampleDevice.COMPACT.config,
    renderExtensions = setOf(GridOverlayRenderExtension())
  )

  @Test
  fun baselineGrid() {
    mugshot.snapshot { MugshotTheme { Screen.PROFILE.content() } }
  }
}

package uk.co.fractalmotion.mugshot.internal.differs

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uk.co.fractalmotion.mugshot.Differ.DiffResult
import javax.imageio.ImageIO

/**
 * The two differs against the same render produced on Linux and on macOS.
 *
 * This is the case the tolerance exists for. The two renders are the same UI, and they disagree on
 * a few dozen pixels because the platforms rasterise text slightly differently. `PixelPerfect`
 * reports that as a failure, `OffByTwo` absorbs it, and the counts below say by how much.
 */
class DiffersTest {
  private val classLoader = DiffersTest::class.java.classLoader

  @Test
  fun `widgets differ across platforms by a handful of pixels`() {
    val linuxWidget = ImageIO.read(classLoader.getResourceAsStream("differs/linux_widget.webp"))
    val macosxWidget = ImageIO.read(classLoader.getResourceAsStream("differs/macosx_widget.webp"))

    val pixelPerfectResult = PixelPerfect.compare(linuxWidget, macosxWidget)
    assertThat(pixelPerfectResult).isInstanceOf(DiffResult.Different::class.java)
    with(pixelPerfectResult as DiffResult.Different) {
      assertThat(percentDifference).isEqualTo(3.5594177E-5f)
      assertThat(numDifferentPixels).isEqualTo(40)
    }

    // The same 40 pixels, all within 2 on every channel, so they are drift rather than change.
    val offByTwoResult = OffByTwo.compare(linuxWidget, macosxWidget)
    assertThat(offByTwoResult).isInstanceOf(DiffResult.Similar::class.java)
    with(offByTwoResult as DiffResult.Similar) {
      assertThat(numSimilarPixels).isEqualTo(40)
    }
  }

  @Test
  fun `full screens differ across platforms by a handful of pixels`() {
    val linuxFullScreen =
      ImageIO.read(classLoader.getResourceAsStream("differs/linux_full_screen.webp"))
    val macosxFullScreen =
      ImageIO.read(classLoader.getResourceAsStream("differs/macosx_full_screen.webp"))

    val pixelPerfectResult = PixelPerfect.compare(linuxFullScreen, macosxFullScreen)
    assertThat(pixelPerfectResult).isInstanceOf(DiffResult.Different::class.java)
    with(pixelPerfectResult as DiffResult.Different) {
      assertThat(percentDifference).isEqualTo(3.9629595E-6f)
      assertThat(numDifferentPixels).isEqualTo(44)
    }

    val offByTwoResult = OffByTwo.compare(linuxFullScreen, macosxFullScreen)
    assertThat(offByTwoResult).isInstanceOf(DiffResult.Similar::class.java)
    with(offByTwoResult as DiffResult.Similar) {
      assertThat(numSimilarPixels).isEqualTo(44)
    }
  }
}

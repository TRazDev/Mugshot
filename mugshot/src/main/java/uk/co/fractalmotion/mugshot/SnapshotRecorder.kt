package uk.co.fractalmotion.mugshot

import uk.co.fractalmotion.mugshot.SnapshotHandler.FrameHandler
import uk.co.fractalmotion.mugshot.internal.ImageUtils
import uk.co.fractalmotion.mugshot.internal.WebpCodec
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Writes each rendered frame to its golden image.
 *
 * The handler used by `recordMugshot*`. Verification uses [SnapshotVerifier], which compares
 * instead of writing and emits the panels a failure report needs.
 */
public class SnapshotRecorder @JvmOverloads constructor(
  private val maxPercentDifference: Double,
  private val differ: Differ = determineDiffer(),
  snapshotRootDirectory: File = File(System.getProperty("mugshot.snapshot.dir"))
) : SnapshotHandler {
  private val goldenImagesDirectory = File(snapshotRootDirectory, "images")

  /**
   * Leaves a golden alone when the render differs from it by less than the threshold.
   *
   * Rendering drifts slightly between machines, so re-recording otherwise rewrites every golden
   * with the current machine's output and buries the real change in the noise.
   */
  private val overwriteOnMaxPercentDifference: Boolean =
    System.getProperty("mugshot.test.record.overwriteOnMaxPercentDifference")?.toBoolean() == true

  init {
    goldenImagesDirectory.mkdirs()
  }

  override fun newFrameHandler(snapshot: Snapshot): FrameHandler =
    object : FrameHandler {
      private val goldenFile =
        File(goldenImagesDirectory, snapshot.toFileName("_", WebpCodec.EXTENSION))

      override fun handle(image: BufferedImage) {
        if (!overwriteOnMaxPercentDifference || !goldenFile.exists()) {
          WebpCodec.encodeTo(goldenFile, image)
          return
        }

        val (_, percentDifference) = ImageUtils.compareImages(
          goldenImage = ImageIO.read(goldenFile),
          image = image,
          differ = differ
        )
        if (percentDifference > maxPercentDifference) {
          WebpCodec.encodeTo(goldenFile, image)
        }
      }

      override fun close(): Unit = Unit
    }

  override fun close(): Unit = Unit
}

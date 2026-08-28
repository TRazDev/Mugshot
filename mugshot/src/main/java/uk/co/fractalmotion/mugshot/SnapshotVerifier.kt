/*
 * Copyright (C) 2020 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paparazzi

import app.cash.paparazzi.Differ
import app.cash.paparazzi.SnapshotHandler.FrameHandler
import app.cash.paparazzi.internal.ImageUtils
import app.cash.paparazzi.internal.WebpCodec
import app.cash.paparazzi.internal.differs.DeltaE2000
import app.cash.paparazzi.internal.differs.Flip
import app.cash.paparazzi.internal.differs.Mssim
import app.cash.paparazzi.internal.differs.OffByTwo
import app.cash.paparazzi.internal.differs.PixelPerfect
import app.cash.paparazzi.internal.differs.Sift
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import javax.imageio.ImageIO

public class SnapshotVerifier @JvmOverloads constructor(
  private val maxPercentDifference: Double,
  rootDirectory: File = File(System.getProperty("paparazzi.snapshot.dir")),
  private val differ: Differ = determineDiffer()
) : SnapshotHandler {
  private val imagesDirectory: File = File(rootDirectory, "images")

  init {
    imagesDirectory.mkdirs()
  }

  override fun newFrameHandler(snapshot: Snapshot): FrameHandler {
    return object : FrameHandler {
      val expected = File(imagesDirectory, snapshot.toFileName(extension = WebpCodec.EXTENSION))

      override fun handle(image: BufferedImage) {
        val goldenImage = if (!expected.exists()) {
          // Stub image for comparison and to proceed with failure output
          BufferedImage(image.width, image.height, TYPE_INT_ARGB)
        } else {
          ImageIO.read(expected)
        } ?: throw NullPointerException(
          """
          Failed to read the snapshot file from the file system.

          If your project uses git LFS, it's possible that it's misconfigured on your machine and
          Paparazzi has just loaded a pointer file instead of the real snapshot file. Follow git
          LFS troubleshooting instructions and try again.

          """.trimIndent()
        )
        ImageUtils.assertImageSimilar(
          relativePath = expected.path,
          image = image,
          goldenImage = goldenImage,
          maxPercentDifferent = maxPercentDifference,
          failureDir = failureDir,
          differ = differ
        )
      }

      override fun close(): Unit = Unit
    }
  }

  override fun close(): Unit = Unit

  private companion object {
    /** Directory where to write the thumbnails and deltas. */
    private val failureDir: File
      get() {
        val path = System.getProperty("paparazzi.failures.dir")
          ?: error("paparazzi.failures.dir system property is required")
        return File(path).apply { mkdirs() }
      }
  }
}

internal fun determineDiffer() =
  System.getProperty("app.cash.paparazzi.differ")?.lowercase().let { differ ->
    when (differ) {
      "offbytwo" -> OffByTwo
      "pixelperfect" -> PixelPerfect
      "mssim" -> Mssim
      "sift" -> Sift
      "flip" -> Flip
      "de2000" -> DeltaE2000
      null, "", "default" -> OffByTwo
      else -> error("Unknown differ type '$differ'.")
    }
  }

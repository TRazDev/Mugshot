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
package uk.co.fractalmotion.mugshot

import uk.co.fractalmotion.mugshot.Differ
import uk.co.fractalmotion.mugshot.SnapshotHandler.FrameHandler
import uk.co.fractalmotion.mugshot.internal.ImageUtils
import uk.co.fractalmotion.mugshot.internal.WebpCodec
import uk.co.fractalmotion.mugshot.internal.differs.OffByTwo
import uk.co.fractalmotion.mugshot.internal.differs.PixelPerfect
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import javax.imageio.ImageIO

public class SnapshotVerifier @JvmOverloads constructor(
  private val maxPercentDifference: Double,
  rootDirectory: File = File(System.getProperty("mugshot.snapshot.dir")),
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
          Mugshot has just loaded a pointer file instead of the real snapshot file. Follow git
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
        val path = System.getProperty("mugshot.failures.dir")
          ?: error("mugshot.failures.dir system property is required")
        return File(path).apply { mkdirs() }
      }
  }
}

internal fun determineDiffer() =
  System.getProperty("uk.co.fractalmotion.mugshot.differ")?.lowercase().let { differ ->
    when (differ) {
      "offbytwo" -> OffByTwo
      "pixelperfect" -> PixelPerfect
      null, "", "default" -> OffByTwo
      else -> error("Unknown differ type '$differ'.")
    }
  }

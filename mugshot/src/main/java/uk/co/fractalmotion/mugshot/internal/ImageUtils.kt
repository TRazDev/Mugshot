/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.fractalmotion.mugshot.internal

import uk.co.fractalmotion.mugshot.Differ
import uk.co.fractalmotion.mugshot.Differ.DiffResult.Different
import uk.co.fractalmotion.mugshot.Differ.DiffResult.Identical
import uk.co.fractalmotion.mugshot.Differ.DiffResult.Similar
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import java.io.File.separatorChar
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max

/**
 * Utilities related to image processing.
 */
internal object ImageUtils {
  @Throws(IOException::class)
  fun assertImageSimilar(
    relativePath: String,
    goldenImage: BufferedImage,
    image: BufferedImage,
    maxPercentDifferent: Double,
    failureDir: File,
    differ: Differ,
    source: String? = null
  ) {
    val (deltaImage, percentDifference) = compareImages(goldenImage, image, differ)

    val goldenImageWidth = goldenImage.width
    val goldenImageHeight = goldenImage.height

    val imageWidth = image.width
    val imageHeight = image.height

    val imageName = getName(relativePath)
    // What failed, named the way a reader recognises it: the screen's own file when the caller
    // knows it, and otherwise the snapshot, which names the test that took it.
    val snapshotName = source ?: imageName.removeSuffix(".${WebpCodec.EXTENSION}")
    var error = when {
      percentDifference > maxPercentDifferent ->
        "$snapshotName differs by %.3f%%".format(percentDifference)

      abs(goldenImageWidth - imageWidth) >= 2 || abs(goldenImageHeight - imageHeight) >= 2 ->
        "$snapshotName is ${imageWidth}x$imageHeight, golden is ${goldenImageWidth}x$goldenImageHeight"

      else -> null
    }

    if (error != null) {
      // The differ builds one wide image: reference, then the pixel difference, then the render.
      // The report shows them as three labelled columns, so they are written out separately here
      // and the label images that used to be composited into the pixels are gone.
      val diffWidth = max(goldenImageWidth, imageWidth)
      writeFailureImage(failureDir, "reference-$imageName", goldenImage)
      writeFailureImage(
        failureDir,
        "diff-$imageName",
        deltaImage.copyOfRegion(goldenImageWidth, diffWidth)
      )

      // The panels above are what the HTML report lays out under its own column headings. The
      // combined image below is what the console error links to, so it keeps the labels drawn
      // into the pixels: opened on its own, nothing else says which panel is which.
      if (diffWidth > 80) {
        /**
         * AWT uses native text rendering under the hood, making it extremely difficult to get
         * consistent cross-platform label text rendering, due to antialiasing, etc. This can
         * result in false negatives when comparing delta images.
         *
         * As a workaround, we instead use text images pre-rendered on MacOSX 14 with the default
         * font=Dialog, size=12 and composite them into the delta image here.
         */
        val g = deltaImage.graphics
        val yOffset = 20 - MAC_OSX_FONT_DIALOG_SIZE_12_ASCENT
        val myClassLoader = ImageUtils::class.java.classLoader!!
        val expectedLabel = ImageIO.read(myClassLoader.getResourceAsStream("expected_label.webp"))
        g.drawImage(expectedLabel, 10, yOffset, null)
        val actualLabel = ImageIO.read(myClassLoader.getResourceAsStream("actual_label.webp"))
        g.drawImage(actualLabel, goldenImageWidth + diffWidth + 10, yOffset, null)
      }

      val deltaOutput = File(failureDir, "delta-$imageName")
      if (deltaOutput.exists()) {
        val deleted = deltaOutput.delete()
        if (!deleted) {
          throw IllegalStateException("Unable to delete $deltaOutput")
        }
      }
      deltaOutput.writeBytes(WebpCodec.encode(deltaImage))

      // The report reads this one back as the "New" panel, so it is written whether or not the
      // message mentions it.
      val actualOutput = File(failureDir, imageName)
      if (actualOutput.exists()) {
        val deleted = actualOutput.delete()
        if (!deleted) {
          throw IllegalStateException("Unable to delete $actualOutput")
        }
      }
      WebpCodec.encodeTo(actualOutput, image)

      // Only what is needed to act on the failure: what changed, the golden to update, and the
      // image showing where it changed. Anything longer stops being read.
      error += "\n  golden: file://${File(relativePath).absolutePath}"
      error += "\n  diff:   file://${deltaOutput.absolutePath}"

      // Thrown without a stack. Every snapshot failure produces the same fifty frames of JUnit
      // and Gradle plumbing, which bury the three lines above and say nothing the message does
      // not: the snapshot names the test, and the report already lists which test failed.
      throw AssertionError(error).apply { stackTrace = emptyArray() }
    }
  }

  @Throws(IOException::class)
  fun compareImages(goldenImage: BufferedImage, image: BufferedImage, differ: Differ): Pair<BufferedImage, Float> {
    var goldenImage = goldenImage
    if (goldenImage.type != TYPE_INT_ARGB) {
      val temp = BufferedImage(goldenImage.width, goldenImage.height, TYPE_INT_ARGB)
      temp.graphics.drawImage(goldenImage, 0, 0, null)
      goldenImage = temp
    }
    if (TYPE_INT_ARGB != goldenImage.type) {
      throw IllegalStateException("expected:<$TYPE_INT_ARGB> but was:<${goldenImage.type}>")
    }

    differ.compare(goldenImage, image).let { result ->
      return when (result) {
        is Identical -> result.delta to 0f
        is Similar -> result.delta to 0f
        is Different -> result.delta to result.percentDifference
      }
    }
  }

  private fun getName(relativePath: String): String {
    return relativePath.substring(relativePath.lastIndexOf(separatorChar) + 1)
  }
}

private const val MAC_OSX_FONT_DIALOG_SIZE_12_ASCENT: Int = 12

/** Writes one panel of a failure comparison, replacing any file from an earlier run. */
private fun writeFailureImage(failureDir: File, name: String, image: BufferedImage) {
  val output = File(failureDir, name)
  if (output.exists() && !output.delete()) {
    throw IllegalStateException("Unable to delete $output")
  }
  WebpCodec.encodeTo(output, image)
}

/**
 * Copies a column out of the comparison image.
 *
 * `getSubimage` returns a view onto the original raster rather than an image starting at (0, 0),
 * and encoding one of those writes from the wrong origin, producing a blank file.
 */
private fun BufferedImage.copyOfRegion(x: Int, width: Int): BufferedImage {
  val region = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  val graphics = region.createGraphics()
  try {
    graphics.drawImage(this, -x, 0, null)
  } finally {
    graphics.dispose()
  }
  return region
}

/*
 * Copyright (C) 2026 Square, Inc.
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
package uk.co.fractalmotion.mugshot.internal

import com.google.common.truth.Truth.assertThat
import com.luciad.imageio.webp.CompressionType
import com.luciad.imageio.webp.WebPWriteParam
import org.junit.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageOutputStream

class WebpCodecTest {
  @Test
  fun roundTripsOpaqueImage() {
    val image = BufferedImage(64, 48, TYPE_INT_RGB).apply {
      for (y in 0 until height) {
        for (x in 0 until width) {
          setRGB(x, y, 0xFF000000.toInt() or (x * 4 shl 16) or (y * 5 shl 8) or 0x7F)
        }
      }
    }

    assertPixelIdentical(expected = image, actual = decode(WebpCodec.encode(image)))
  }

  @Test
  fun roundTripsTranslucentImage() {
    val image = BufferedImage(32, 32, TYPE_INT_ARGB).apply {
      for (y in 0 until height) {
        for (x in 0 until width) {
          setRGB(x, y, (x * 8 shl 24) or (0xAA shl 16) or (y * 8 shl 8) or 0x33)
        }
      }
    }

    assertPixelIdentical(expected = image, actual = decode(WebpCodec.encode(image)))
  }

  /**
   * Regression test for [com.luciad.imageio.webp.WebPWriteParam.exact]. Without it libwebp rewrites
   * the RGB channels of fully-transparent pixels, and every Differ compares via
   * [BufferedImage.getRGB], which reads those channels regardless of alpha.
   */
  @Test
  fun preservesRgbUnderFullyTransparentPixels() {
    val image = BufferedImage(16, 16, TYPE_INT_ARGB).apply {
      for (y in 0 until height) {
        for (x in 0 until width) {
          // alpha = 0, but each pixel carries a distinct, non-zero RGB value.
          setRGB(x, y, (0x00 shl 24) or (0xFF shl 16) or (x * 16 shl 8) or (y * 16))
        }
      }
    }

    assertPixelIdentical(expected = image, actual = decode(WebpCodec.encode(image)))
  }

  /**
   * [WebpCodec] deliberately does not max out `method`/`compressionQuality`, because maxing them is
   * ~36x slower for under 5% of size. This pins down why that is safe: under
   * [CompressionType.Lossless] those two are effort knobs only — they change how hard the encoder
   * searches for a shorter encoding, never the pixels it reproduces. If anyone ever switches the
   * codec to lossy, this fails.
   */
  @Test
  fun effortSettingsDoNotAffectFidelity() {
    // Fully-transparent RGB is the content most sensitive to encoder settings.
    val image = BufferedImage(48, 48, TYPE_INT_ARGB).apply {
      for (y in 0 until height) {
        for (x in 0 until width) {
          val alpha = if ((x + y) % 3 == 0) 0x00 else 0xFF
          setRGB(x, y, (alpha shl 24) or (0xFF shl 16) or (x * 5 shl 8) or (y * 5))
        }
      }
    }

    val sizes = listOf(0 to 0f, 4 to 0.5f, 6 to 1f).map { (method, quality) ->
      val encoded = encodeWithEffort(image, method, quality)
      assertPixelIdentical(expected = image, actual = decode(encoded))
      encoded.size
    }

    // Effort genuinely varied the encoding, so the assertions above weren't comparing identical
    // bytes. Not every level need differ — on small images the higher ones often converge.
    assertThat(sizes.toSet().size).isGreaterThan(1)
  }

  private fun encodeWithEffort(image: BufferedImage, method: Int, quality: Float): ByteArray {
    val writer = ImageIO.getImageWritersByMIMEType("image/webp").next()
    val param = (writer.defaultWriteParam as WebPWriteParam).apply {
      compressionType = CompressionType.Lossless
      compressionQuality = quality
      this.method = method
      exact = true
    }
    val bytes = ByteArrayOutputStream()
    try {
      MemoryCacheImageOutputStream(bytes).use { output ->
        writer.output = output
        writer.write(null, IIOImage(image, null, null), param)
      }
    } finally {
      writer.dispose()
    }
    return bytes.toByteArray()
  }

  @Test
  fun encodingIsDeterministic() {
    val image = BufferedImage(40, 40, TYPE_INT_ARGB).apply {
      for (y in 0 until height) {
        for (x in 0 until width) {
          setRGB(x, y, 0xFF000000.toInt() or (x * 6 shl 16) or (y * 6 shl 8) or (x xor y))
        }
      }
    }

    assertThat(WebpCodec.encode(image)).isEqualTo(WebpCodec.encode(image))
  }

  private fun decode(bytes: ByteArray): BufferedImage =
    checkNotNull(ImageIO.read(bytes.inputStream())) { "No ImageIO reader could decode the WebP" }

  private fun assertPixelIdentical(expected: BufferedImage, actual: BufferedImage) {
    assertThat(actual.width).isEqualTo(expected.width)
    assertThat(actual.height).isEqualTo(expected.height)
    for (y in 0 until expected.height) {
      for (x in 0 until expected.width) {
        // Compared as hex strings so a failure names the pixel and both ARGB values.
        assertThat("($x,$y)=${Integer.toHexString(actual.getRGB(x, y))}")
          .isEqualTo("($x,$y)=${Integer.toHexString(expected.getRGB(x, y))}")
      }
    }
  }
}

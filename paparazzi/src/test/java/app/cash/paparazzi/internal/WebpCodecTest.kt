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
package app.cash.paparazzi.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.BufferedImage.TYPE_INT_RGB
import javax.imageio.ImageIO

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

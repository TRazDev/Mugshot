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

import com.luciad.imageio.webp.CompressionType
import com.luciad.imageio.webp.WebPWriteParam
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriter
import javax.imageio.stream.MemoryCacheImageOutputStream

/**
 * Encodes snapshots as lossless WebP.
 *
 * This is the only encode path in the codebase: the recorded golden and the "actual" file written
 * on a failed comparison must be byte-identical for the same pixels, and they are produced from
 * different call sites.
 */
internal object WebpCodec {
  const val EXTENSION: String = "webp"

  private const val MIME_TYPE = "image/webp"

  /**
   * Encodes [image] as a lossless WebP.
   *
   * [WebPWriteParam.exact] is required, not an optimization. Without it libwebp is free to rewrite
   * the RGB channels of fully-transparent pixels for better compression, and every [uk.co.fractalmotion.mugshot.Differ]
   * compares via [BufferedImage.getRGB], which reads those channels even where alpha is zero. A
   * golden and a fresh render of the same view would then differ on invisible pixels.
   */
  fun encode(image: BufferedImage): ByteArray {
    val writer = newWriter()
    val param = (writer.defaultWriteParam as WebPWriteParam).apply {
      compressionType = CompressionType.Lossless
      // Under Lossless, these two are pure effort knobs — how hard the encoder searches for a
      // shorter encoding of the same pixels. They never affect fidelity, so tuning them cannot
      // weaken snapshot comparison.
      //
      // They are deliberately not maxed out. Measured over the 145 goldens in this repo, method 6
      // with quality 1f takes 566ms/image, while every other point in the method 0-6 / quality
      // 0-1 grid lands between 7 and 50ms: that one combination trips libwebp's exhaustive
      // backward-reference search, costing 24x the time of method 6 / quality 0.75f to save 2.7%
      // of bytes. Total size varies under 5% across the whole grid, so the effort isn't worth it —
      // at these settings encoding is faster than the PNG encoder this replaced.
      compressionQuality = 0.5f
      method = 4
      exact = true
    }

    val bytes = ByteArrayOutputStream()
    try {
      MemoryCacheImageOutputStream(bytes).use { output ->
        writer.output = output
        writer.write(null, IIOImage(image.toArgb(), null, null), param)
      }
    } finally {
      writer.dispose()
    }
    return bytes.toByteArray()
  }

  fun encodeTo(file: File, image: BufferedImage) {
    file.writeBytes(encode(image))
  }

  private fun newWriter(): ImageWriter {
    val writers = ImageIO.getImageWritersByMIMEType(MIME_TYPE)
    if (writers.hasNext()) return writers.next()

    // Mugshot runs under layoutlib's class loaders, where ImageIO's initial service lookup may
    // not have seen the WebP plugin.
    ImageIO.scanForPlugins()
    val rescanned = ImageIO.getImageWritersByMIMEType(MIME_TYPE)
    check(rescanned.hasNext()) {
      "No WebP ImageIO writer found. Is com.github.usefulness:webp-imageio on the test runtime classpath?"
    }
    return rescanned.next()
  }

  /**
   * The encoder picks its RGB vs RGBA path from the color model, and reads pixels through the
   * raster. Normalizing up front keeps both stable regardless of what layoutlib handed us.
   */
  private fun BufferedImage.toArgb(): BufferedImage {
    if (type == TYPE_INT_ARGB) return this
    return BufferedImage(width, height, TYPE_INT_ARGB).also { copy ->
      copy.createGraphics().apply {
        composite = java.awt.AlphaComposite.Src
        drawImage(this@toArgb, 0, 0, null)
        dispose()
      }
    }
  }
}

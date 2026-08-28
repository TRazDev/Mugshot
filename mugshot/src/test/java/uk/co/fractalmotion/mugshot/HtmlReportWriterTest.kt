/*
 * Copyright (C) 2019 Square, Inc.
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

import uk.co.fractalmotion.mugshot.FileSubject.Companion.assertThat
import uk.co.fractalmotion.mugshot.internal.ImageUtils
import uk.co.fractalmotion.mugshot.internal.differs.PixelPerfect
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.Date

class HtmlReportWriterTest {
  @get:Rule
  val reportRoot: TemporaryFolder = TemporaryFolder()

  @get:Rule
  val snapshotRoot: TemporaryFolder = TemporaryFolder()

  private val anyImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
  private val otherImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).apply {
    setRGB(0, 0, 0xFFFFFFFF.toInt())
  }
  private val anyImageHash = "b82e377b94340f336f7b4eb7b7358e5552727efd"

  @Test
  fun happyPathImages() {
    val htmlReportWriter = HtmlReportWriter(
      runName = "run_one",
      rootDirectory = reportRoot.root,
      maxPercentDifference = 0.0,
      differ = PixelPerfect,
      snapshotRootDirectory = snapshotRoot.root
    )
    htmlReportWriter.use {
      val frameHandler = htmlReportWriter.newFrameHandler(
        snapshot = Snapshot(
          name = "loading",
          testName = TestName("uk.co.fractalmotion.mugshot", "CelebrityTest", "testSettings"),
          timestamp = Instant.parse("2019-03-20T10:27:43Z").toDate(),
          tags = listOf("redesign")
        )
      )
      frameHandler.use {
        frameHandler.handle(anyImage)
      }
    }

    assertThat(File("${reportRoot.root}/index.js")).hasContent(
      """
        |window.all_runs = [
        |  "run_one"
        |];
      """.trimMargin()
    )

    assertThat(File("${reportRoot.root}/runs/run_one.js")).hasContent(
      """
        |window.runs["run_one"] = [
        |  {
        |    "name": "loading",
        |    "testName": "uk.co.fractalmotion.mugshot.CelebrityTest#testSettings",
        |    "timestamp": "2019-03-20T10:27:43.000Z",
        |    "tags": [
        |      "redesign"
        |    ],
        |    "file": "images/$anyImageHash.webp"
        |  }
        |];
      """.trimMargin()
    )
  }

  @Test
  fun failureActualImageMatchesRecordedGoldenImageBytes() {
    try {
      System.setProperty("mugshot.test.record", "true")

      val htmlReportWriter = HtmlReportWriter(
        runName = "record_run",
        rootDirectory = reportRoot.root,
        maxPercentDifference = 0.0,
        differ = PixelPerfect,
        snapshotRootDirectory = snapshotRoot.root
      )
      val snapshot = Snapshot(
        name = "test",
        testName = TestName("uk.co.fractalmotion.mugshot", "HomeView", "testSettings"),
        timestamp = Instant.parse("2021-02-23T10:27:43Z").toDate()
      )
      val golden = File("${snapshotRoot.root}/images/uk.co.fractalmotion.mugshot_HomeView_testSettings_test.webp")

      htmlReportWriter.use {
        htmlReportWriter.newFrameHandler(snapshot = snapshot).use { frameHandler ->
          frameHandler.handle(otherImage)
        }
      }

      val failureDir = reportRoot.newFolder("failures")
      assertThrows(AssertionError::class.java) {
        ImageUtils.assertImageSimilar(
          relativePath = golden.path,
          goldenImage = anyImage,
          image = otherImage,
          maxPercentDifferent = 0.0,
          failureDir = failureDir,
          differ = PixelPerfect
        )
      }

      assertThat(File(failureDir, golden.name).readBytes()).isEqualTo(golden.readBytes())
    } finally {
      System.clearProperty("mugshot.test.record")
    }
  }

  @Test
  fun sanitizeForFilename() {
    assertThat("0 Dollars".sanitizeForFilename()).isEqualTo("0_dollars")
    assertThat("`!#$%&*+=|\\'\"<>?/".sanitizeForFilename()).isEqualTo("_________________")
    assertThat("~@^()[]{}:;,.".sanitizeForFilename()).isEqualTo("~@^()[]{}:;,.")
  }

  @Test
  fun noSnapshotOnFailure() {
    val htmlReportWriter = HtmlReportWriter(
      runName = "run_one",
      rootDirectory = reportRoot.root,
      maxPercentDifference = 0.0,
      differ = PixelPerfect,
      snapshotRootDirectory = snapshotRoot.root
    )
    htmlReportWriter.use {
      val frameHandler = htmlReportWriter.newFrameHandler(
        snapshot = Snapshot(
          name = "loading",
          testName = TestName("uk.co.fractalmotion.mugshot", "CelebrityTest", "testSettings"),
          timestamp = Instant.parse("2019-03-20T10:27:43Z").toDate()
        )
      )
      frameHandler.use {
        // intentionally empty, to simulate no content written on exception
      }
    }

    assertThat(File(reportRoot.root, "images")).isEmptyDirectory()
  }

  @Test
  fun imagesAlwaysOverwriteOnRecord() {
    try {
      // set record mode
      System.setProperty("mugshot.test.record", "true")

      val htmlReportWriter = HtmlReportWriter(
        runName = "record_run",
        rootDirectory = reportRoot.root,
        maxPercentDifference = 0.0,
        differ = PixelPerfect,
        snapshotRootDirectory = snapshotRoot.root
      )
      htmlReportWriter.use {
        val now = Instant.parse("2021-02-23T10:27:43Z")
        val snapshot = Snapshot(
          name = "test",
          testName = TestName("uk.co.fractalmotion.mugshot", "HomeView", "testSettings"),
          timestamp = now.toDate()
        )
        val golden =
          File("${snapshotRoot.root}/images/uk.co.fractalmotion.mugshot_HomeView_testSettings_test.webp")

        // precondition
        assertThat(golden).doesNotExist()

        // take 1
        val frameHandler1 = htmlReportWriter.newFrameHandler(snapshot = snapshot)
        frameHandler1.use { frameHandler1.handle(anyImage) }
        assertThat(golden).exists()
        val timeFirstWrite = golden.lastModifiedTime()

        // I know....but guarantees writes won't happen in same tick
        Thread.sleep(100)

        // take 2
        val frameHandler2 = htmlReportWriter.newFrameHandler(
          snapshot = snapshot.copy(timestamp = now.plusSeconds(1).toDate())
        )
        frameHandler2.use { frameHandler2.handle(anyImage) }
        assertThat(golden).exists()
        val timeOverwrite = golden.lastModifiedTime()

        // should always overwrite
        assertThat(timeOverwrite).isGreaterThan(timeFirstWrite)
      }
    } finally {
      System.clearProperty("mugshot.test.record")
    }
  }

  @Test
  fun imagesDoesntOverwriteOnRecordWithFlag() {
    try {
      // set record mode
      System.setProperty("mugshot.test.record", "true")
      System.setProperty("mugshot.test.record.overwriteOnMaxPercentDifference", "true")

      val htmlReportWriter = HtmlReportWriter(
        runName = "record_run",
        rootDirectory = reportRoot.root,
        maxPercentDifference = 0.0,
        differ = PixelPerfect,
        snapshotRootDirectory = snapshotRoot.root
      )
      htmlReportWriter.use {
        val now = Instant.parse("2021-02-23T10:27:43Z")
        val snapshot = Snapshot(
          name = "test",
          testName = TestName("uk.co.fractalmotion.mugshot", "HomeView", "testSettings"),
          timestamp = now.toDate()
        )
        val golden =
          File("${snapshotRoot.root}/images/uk.co.fractalmotion.mugshot_HomeView_testSettings_test.webp")

        // precondition
        assertThat(golden).doesNotExist()

        // take 1
        val frameHandler1 = htmlReportWriter.newFrameHandler(snapshot = snapshot)
        frameHandler1.use { frameHandler1.handle(anyImage) }
        assertThat(golden).exists()
        val timeFirstWrite = golden.lastModifiedTime()

        // I know....but guarantees writes won't happen in same tick
        Thread.sleep(100)

        // take 2
        val frameHandler2 = htmlReportWriter.newFrameHandler(
          snapshot = snapshot.copy(timestamp = now.plusSeconds(1).toDate())
        )
        frameHandler2.use { frameHandler2.handle(anyImage) }
        assertThat(golden).exists()
        val timeOverwrite = golden.lastModifiedTime()

        // should always overwrite
        assertThat(timeOverwrite).isEqualTo(timeFirstWrite)
      }
    } finally {
      System.clearProperty("mugshot.test.record")
      System.clearProperty("mugshot.test.record.overwriteOnMaxPercentDifference")
    }
  }

  @Test
  fun imagesOverwriteOnRecordWithFlagAndImageDiff() {
    try {
      // set record mode
      System.setProperty("mugshot.test.record", "true")
      System.setProperty("mugshot.test.record.overwriteOnMaxPercentDifference", "true")

      val htmlReportWriter = HtmlReportWriter(
        runName = "record_run",
        rootDirectory = reportRoot.root,
        maxPercentDifference = 0.0,
        differ = PixelPerfect,
        snapshotRootDirectory = snapshotRoot.root
      )
      htmlReportWriter.use {
        val now = Instant.parse("2021-02-23T10:27:43Z")
        val snapshot = Snapshot(
          name = "test",
          testName = TestName("uk.co.fractalmotion.mugshot", "HomeView", "testSettings"),
          timestamp = now.toDate()
        )
        val golden =
          File("${snapshotRoot.root}/images/uk.co.fractalmotion.mugshot_HomeView_testSettings_test.webp")

        // precondition
        assertThat(golden).doesNotExist()

        // take 1
        val frameHandler1 = htmlReportWriter.newFrameHandler(snapshot = snapshot)
        frameHandler1.use { frameHandler1.handle(anyImage) }
        assertThat(golden).exists()
        val timeFirstWrite = golden.lastModifiedTime()

        // I know....but guarantees writes won't happen in same tick
        Thread.sleep(100)

        // take 2
        val frameHandler2 = htmlReportWriter.newFrameHandler(
          snapshot = snapshot.copy(timestamp = now.plusSeconds(1).toDate())
        )
        frameHandler2.use { frameHandler2.handle(otherImage) }
        assertThat(golden).exists()
        val timeOverwrite = golden.lastModifiedTime()

        // should always overwrite
        assertThat(timeOverwrite).isGreaterThan(timeFirstWrite)
      }
    } finally {
      System.clearProperty("mugshot.test.record")
      System.clearProperty("mugshot.test.record.overwriteOnMaxPercentDifference")
    }
  }

  private fun Instant.toDate() = Date(toEpochMilli())

  private fun File.lastModifiedTime(): FileTime {
    return Files.readAttributes(this.toPath(), BasicFileAttributes::class.java).lastModifiedTime()
  }
}

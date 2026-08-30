package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * `recordMugshot*` -- writing golden images.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class RecordModeTest : MugshotPluginTestCase() {
  @Test
  fun record() {
    val fixtureRoot = File("src/test/projects/record-mode")

    val result = gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":testDebugUnitTest")).isNotNull()

    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")
    assertThat(snapshot.exists()).isTrue()

    val snapshotWithLabel =
      File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record_label.webp")
    assertThat(snapshotWithLabel.exists()).isTrue()
  }

  @Test
  fun recordAllVariants() {
    val fixtureRoot = File("src/test/projects/record-mode")
    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val result = gradleRunner
      .withArguments("recordMugshot", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":recordMugshotDebug")).isNotNull()
    assertThat(result.task(":recordMugshotRelease")).isNotNull()
  }

  @Test
  fun recordMultiModuleProject() {
    val fixtureRoot = File("src/test/projects/record-mode-multiple-modules")
    val moduleRoot = File(fixtureRoot, "module")

    val result = gradleRunner
      .withArguments("module:recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":module:testDebugUnitTest")).isNotNull()

    val snapshotsDir = File(moduleRoot, "src/test/snapshots").registerForDeletionOnExit()

    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")
    assertThat(snapshot.exists()).isTrue()

    val snapshotWithLabel =
      File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record_label.webp")
    assertThat(snapshotWithLabel.exists()).isTrue()
  }

  @Test
  fun recordModeSingleTestOfMany() {
    val fixtureRoot = File("src/test/projects/record-mode-multiple-tests")
    val moduleRoot = File(fixtureRoot, "module")

    val result = gradleRunner
      .withArguments("module:recordMugshotDebug", "--tests=*recordSecond", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":module:testDebugUnitTest")).isNotNull()

    val snapshotsDir = File(moduleRoot, "src/test/snapshots").registerForDeletionOnExit()

    val firstSnapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_recordFirst.webp")
    assertThat(firstSnapshot.exists()).isFalse()

    val secondSnapshot =
      File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_recordSecond_label.webp")
    assertThat(secondSnapshot.exists()).isTrue()
  }

  @Test
  fun cleanRecord() {
    val fixtureRoot = File("src/test/projects/clean-record")
    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val snapshotName1 = "uk.co.fractalmotion.mugshot.plugin.test_CleanRecordTest_clean.webp"
    val snapshotName2 = "uk.co.fractalmotion.mugshot.plugin.test_CleanRecordTest_clean_keep.webp"
    val firstGoldenFile = File(fixtureRoot, "src/test/resources/$snapshotName1")
    val secondGoldenFile = File(fixtureRoot, "src/test/resources/$snapshotName2")

    val snapshotToBeDeleted = File(snapshotsDir, "images/$snapshotName1")
    val snapshotToBeKept = File(snapshotsDir, "images/$snapshotName2")

    firstGoldenFile.copyTo(snapshotToBeDeleted, overwrite = false)
    secondGoldenFile.copyTo(snapshotToBeKept, overwrite = false)

    assertThat(snapshotToBeDeleted.exists()).isTrue()
    assertThat(snapshotToBeKept.exists()).isTrue()

    val result = gradleRunner
      .withArguments("cleanRecordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":deleteMugshotSnapshots")).isNotNull()
    assertThat(result.task(":recordMugshotDebug")).isNotNull()

    assertThat(snapshotToBeDeleted.exists()).isFalse()
    assertThat(snapshotToBeKept.exists()).isTrue()
  }

  @Test
  fun deleteSnapshots() {
    val fixtureRoot = File("src/test/projects/delete-snapshots")
    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val snapshotName1 = "uk.co.fractalmotion.mugshot.plugin.test_DeleteTest_delete.webp"
    val snapshotName2 = "uk.co.fractalmotion.mugshot.plugin.test_DeleteTest_delete_label.webp"
    val firstGoldenFile = File(fixtureRoot, "src/test/resources/$snapshotName1")
    val secondGoldenFile = File(fixtureRoot, "src/test/resources/$snapshotName2")

    val snapshot = File(snapshotsDir, "images/$snapshotName1")
    val snapshotWithLabel = File(snapshotsDir, "images/$snapshotName2")

    firstGoldenFile.copyTo(snapshot, overwrite = false)
    secondGoldenFile.copyTo(snapshotWithLabel, overwrite = false)

    assertThat(snapshot.exists()).isTrue()
    assertThat(snapshotWithLabel.exists()).isTrue()

    gradleRunner
      .withArguments("deleteMugshotSnapshots", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(snapshot.exists()).isFalse()
    assertThat(snapshotWithLabel.exists()).isFalse()
  }

  @Test
  fun overwriteSnapshotOnMaxPercentDiff() {
    val fixtureRoot = File("src/test/projects/overwrite-on-max-percent-difference").clearNestedBuildState()

    val dontRecordFile =
      File(fixtureRoot, "src/test/snapshots/images/uk.co.fractalmotion.mugshot.plugin.test_RecordSnapshotTest_dontRecord.webp")
        .registerForRestoreOnExit()
    val dontRecordLastModified = dontRecordFile.lastModified()
    val recordFile =
      File(fixtureRoot, "src/test/snapshots/images/uk.co.fractalmotion.mugshot.plugin.test_RecordSnapshotTest_record.webp")
        .registerForRestoreOnExit()
    val recordLastModified = recordFile.lastModified()

    gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(dontRecordLastModified).isEqualTo(dontRecordFile.lastModified())
    assertThat(recordLastModified).isNotEqualTo(recordFile.lastModified())
  }

  @Test
  fun similarImagesProduceUniqueSnapshots() {
    val fixtureRoot = File("src/test/projects/similar-images")

    gradleRunner
      .withArguments("recordMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    val reportsDir = File(fixtureRoot, "build/reports/mugshot/debug/images")
    assertThat(reportsDir.listFiles()!!).hasLength(3)
  }
}

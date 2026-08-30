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
    val fixtureRoot = fixture("record-mode")

    val result = fixtureRoot.runBuild("recordMugshotDebug")

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
    val fixtureRoot = fixture("record-mode")
    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val result = fixtureRoot.runBuild("recordMugshot")

    assertThat(result.task(":recordMugshotDebug")).isNotNull()
    assertThat(result.task(":recordMugshotRelease")).isNotNull()
  }

  @Test
  fun recordMultiModuleProject() {
    val fixtureRoot = fixture("record-mode-multiple-modules")
    val moduleRoot = File(fixtureRoot, "module")

    val result = fixtureRoot.runBuild("module:recordMugshotDebug")

    assertThat(result.task(":module:testDebugUnitTest")).isNotNull()

    val snapshotsDir = File(moduleRoot, "src/test/snapshots").registerForDeletionOnExit()

    val snapshot = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record.webp")
    assertThat(snapshot.exists()).isTrue()

    val snapshotWithLabel =
      File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_record_label.webp")
    assertThat(snapshotWithLabel.exists()).isTrue()
  }

  @Test
  fun recordModeTestsFilterMatchesMoreThanOne() {
    val fixtureRoot = fixture("record-mode-multiple-tests")
    val moduleRoot = File(fixtureRoot, "module")
    val snapshotsDir = File(moduleRoot, "src/test/snapshots").registerForDeletionOnExit()

    // A glob that is not just a suffix, and that matches both tests in the fixture.
    fixtureRoot.runBuild("module:recordMugshotDebug", "--tests=*RecordTest.record*")

    val first = File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_recordFirst.webp")
    val second =
      File(snapshotsDir, "images/uk.co.fractalmotion.mugshot.plugin.test_RecordTest_recordSecond_label.webp")
    assertThat(first.exists()).isTrue()
    assertThat(second.exists()).isTrue()
  }

  @Test
  fun recordModeSingleTestOfMany() {
    val fixtureRoot = fixture("record-mode-multiple-tests")
    val moduleRoot = File(fixtureRoot, "module")

    val result = fixtureRoot.runBuild("module:recordMugshotDebug", "--tests=*recordSecond")

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
    val fixtureRoot = fixture("clean-record")
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

    val result = fixtureRoot.runBuild("cleanRecordMugshotDebug")

    assertThat(result.task(":deleteMugshotSnapshots")).isNotNull()
    assertThat(result.task(":recordMugshotDebug")).isNotNull()

    assertThat(snapshotToBeDeleted.exists()).isFalse()
    assertThat(snapshotToBeKept.exists()).isTrue()
  }

  @Test
  fun cleanRecordAllVariants() {
    // record-mode rather than clean-record: it sets
    // android.onlyEnableUnitTestForTheTestedBuildType=false, so AGP creates a unit test -- and
    // therefore Mugshot tasks -- for release as well as debug.
    val fixtureRoot = fixture("record-mode")
    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val result = fixtureRoot.runBuild("cleanRecordMugshot")

    assertThat(result.task(":cleanRecordMugshotDebug")).isNotNull()
    assertThat(result.task(":cleanRecordMugshotRelease")).isNotNull()
  }

  @Test
  fun deleteSnapshotsForSingleVariant() {
    val fixtureRoot = fixture("delete-snapshots")
    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    val snapshotName = "uk.co.fractalmotion.mugshot.plugin.test_DeleteTest_delete.webp"
    val snapshot = File(snapshotsDir, "images/$snapshotName")
    File(fixtureRoot, "src/test/resources/$snapshotName").copyTo(snapshot, overwrite = false)
    assertThat(snapshot.exists()).isTrue()

    val result = fixtureRoot.runBuild("deleteDebugMugshotSnapshots")

    result.assertTaskSucceeded(":deleteDebugMugshotSnapshots")
    assertThat(snapshot.exists()).isFalse()
  }

  @Test
  fun deleteSnapshots() {
    val fixtureRoot = fixture("delete-snapshots")
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

    fixtureRoot.runBuild("deleteMugshotSnapshots")

    assertThat(snapshot.exists()).isFalse()
    assertThat(snapshotWithLabel.exists()).isFalse()
  }

  @Test
  fun overwriteSnapshotOnMaxPercentDiff() {
    val fixtureRoot = fixture("overwrite-on-max-percent-difference").clearNestedBuildState()

    val dontRecordFile =
      File(fixtureRoot, "src/test/snapshots/images/uk.co.fractalmotion.mugshot.plugin.test_RecordSnapshotTest_dontRecord.webp")
        .registerForRestoreOnExit()
    val dontRecordLastModified = dontRecordFile.lastModified()
    val recordFile =
      File(fixtureRoot, "src/test/snapshots/images/uk.co.fractalmotion.mugshot.plugin.test_RecordSnapshotTest_record.webp")
        .registerForRestoreOnExit()
    val recordLastModified = recordFile.lastModified()

    fixtureRoot.runBuild("recordMugshotDebug")

    assertThat(dontRecordLastModified).isEqualTo(dontRecordFile.lastModified())
    assertThat(recordLastModified).isNotEqualTo(recordFile.lastModified())
  }

  @Test
  fun similarImagesProduceUniqueSnapshots() {
    val fixtureRoot = fixture("similar-images")

    fixtureRoot.runBuild("recordMugshotDebug")

    val reportsDir = File(fixtureRoot, "build/reports/mugshot/debug/images")
    assertThat(reportsDir.listFiles()!!).hasLength(3)
  }
}

package uk.co.fractalmotion.mugshot

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

class SnapshotFileNameTest {
  @Test
  fun `an ordinary name is left alone`() {
    val name = snapshot(method = "rendersTheThing").toFileName(extension = "webp")

    assertThat(name)
      .isEqualTo("com.example_ExampleTest_rendersTheThing.webp")
  }

  @Test
  fun `a name a filesystem would reject is shortened`() {
    val name = snapshot(method = "a".repeat(500)).toFileName(extension = "webp")

    // 255 is the limit a single name has to live under; the cap leaves room below it.
    assertThat(name.length).isAtMost(200)
    assertThat(name).endsWith(".webp")
    assertThat(name).startsWith("com.example_ExampleTest_aaa")
  }

  @Test
  fun `shortening is the same on every machine and every run`() {
    val once = snapshot(method = "a".repeat(500)).toFileName(extension = "webp")
    val again = snapshot(method = "a".repeat(500)).toFileName(extension = "webp")

    // A golden recorded on one machine is verified on another, so this cannot vary.
    assertThat(once).isEqualTo(again)
  }

  @Test
  fun `two long names that begin alike stay apart`() {
    val first = snapshot(method = "a".repeat(500) + "first").toFileName(extension = "webp")
    val second = snapshot(method = "a".repeat(500) + "second").toFileName(extension = "webp")

    assertThat(first).isNotEqualTo(second)
  }

  private fun snapshot(method: String) =
    Snapshot(
      name = null,
      testName = TestName("com.example", "ExampleTest", method),
      timestamp = Date(0)
    )
}

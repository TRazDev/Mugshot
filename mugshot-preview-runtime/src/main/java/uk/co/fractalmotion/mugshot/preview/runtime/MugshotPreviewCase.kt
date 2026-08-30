package uk.co.fractalmotion.mugshot.preview.runtime

import androidx.compose.runtime.Composable

/**
 * One generated screenshot: a composable, the configuration to render it at, and its name.
 *
 * [toString] returns [snapshotName] because the generated test parameterises over these with
 * `@Parameterized.Parameters(name = "{0}")` — the name lands in the JUnit test name and therefore
 * in the golden image filename. A data class would synthesise a `toString` containing the lambda's
 * identity hash, which changes every run.
 */
public class MugshotPreviewCase(
  public val snapshotName: String,
  public val config: MugshotPreviewConfig,
  private val frames: () -> List<@Composable () -> Unit>
) {
  /**
   * The composables to snapshot, in order.
   *
   * A list rather than a single lambda so a `@PreviewParameter` preview can fan out: its provider
   * is arbitrary Kotlin whose value count is not knowable at compile time, so the expansion has to
   * happen here, at test runtime. One frame for an ordinary preview, one per value otherwise.
   */
  public fun frames(): List<@Composable () -> Unit> = frames.invoke()

  override fun toString(): String = snapshotName
}

/**
 * Expands a `@PreviewParameter` provider's values into one frame each.
 *
 * Called from generated code; `values` is the provider's `values` sequence.
 */
public fun <T> parameterizedFrames(
  values: Sequence<T>,
  limit: Int,
  composable: @Composable (T) -> Unit
): List<@Composable () -> Unit> =
  values
    .take(limit)
    .map<T, @Composable () -> Unit> { value -> { composable(value) } }
    .toList()

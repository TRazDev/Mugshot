package uk.co.fractalmotion.mugshot.preview.runtime

import androidx.compose.runtime.Composable

/**
 * Expands the values of a `@PreviewParameter` provider into one [MugshotPreviewData] per value.
 *
 * The preview processor cannot enumerate a `PreviewParameterProvider` at compile time — its values
 * are arbitrary Kotlin that only exists once the test JVM is running — so generated code defers to
 * this function instead, passing `provider.values` straight through.
 *
 * Snapshots are named by the value's position (`_0`, `_1`, …) rather than by the value itself,
 * because a value's `toString()` is not safe to put in a golden image filename.
 */
public fun <T> parameterizedPreviews(
  snapshotName: String,
  values: Sequence<T>,
  limit: Int,
  composable: @Composable (T) -> Unit
): List<MugshotPreviewData> =
  values
    .take(limit)
    .mapIndexed { index, value ->
      MugshotPreviewData(
        snapshotName = "${snapshotName}_$index",
        composable = { composable(value) }
      )
    }
    .toList()

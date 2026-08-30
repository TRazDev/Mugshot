package uk.co.fractalmotion.mugshot.sample.designsystem.foundation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The sample's only source of gradients.
 *
 * They are deliberately confined to bounded elements — hero panels, artwork tiles, avatars — and
 * never used as a page background. A full bleed gradient is both the largest contributor to golden
 * file size in lossless WebP and the most likely thing to dither differently between CI platforms.
 */
internal object MugshotGradients {
  @Composable
  @ReadOnlyComposable
  fun hero(): Brush =
    Brush.linearGradient(
      listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.mix(MaterialTheme.colorScheme.tertiary, 0.55f)
      )
    )

  @Composable
  @ReadOnlyComposable
  fun cool(): Brush =
    Brush.linearGradient(
      listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
    )

  @Composable
  @ReadOnlyComposable
  fun warm(): Brush =
    Brush.linearGradient(
      listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.error)
    )

  @Composable
  @ReadOnlyComposable
  fun muted(): Brush =
    Brush.linearGradient(
      listOf(
        MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.colorScheme.surfaceContainer
      )
    )

  /** Deterministic pick, so a given item always draws the same artwork backdrop. */
  @Composable
  @ReadOnlyComposable
  fun forIndex(index: Int): Brush =
    when (index.mod(4)) {
      0 -> hero()
      1 -> cool()
      2 -> warm()
      else -> muted()
    }
}

private fun Color.mix(other: Color, fraction: Float): Color =
  Color(
    red = red + (other.red - red) * fraction,
    green = green + (other.green - green) * fraction,
    blue = blue + (other.blue - blue) * fraction,
    alpha = alpha
  )

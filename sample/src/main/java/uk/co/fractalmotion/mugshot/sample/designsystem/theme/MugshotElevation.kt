package uk.co.fractalmotion.mugshot.sample.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tonal elevation only.
 *
 * Material's tonal elevation is a flat colour shift, whereas shadow elevation is a Gaussian blur —
 * the least deterministic thing layoutlib draws, and the most likely to make goldens disagree
 * between the macOS, Windows and Linux CI legs. Components here pass these to `tonalElevation` and
 * leave `shadowElevation` at zero.
 */
internal object MugshotElevation {
  val level0: Dp = 0.dp
  val level1: Dp = 1.dp
  val level2: Dp = 3.dp
  val level3: Dp = 6.dp
  val level4: Dp = 8.dp
  val level5: Dp = 12.dp
}

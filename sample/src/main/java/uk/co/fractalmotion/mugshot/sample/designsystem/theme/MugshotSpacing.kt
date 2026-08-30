package uk.co.fractalmotion.mugshot.sample.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The 4dp grid every screen in the sample lays out on. */
@Immutable
internal data class MugshotSpacing(
  val hairline: Dp = 2.dp,
  val extraSmall: Dp = 4.dp,
  val small: Dp = 8.dp,
  val medium: Dp = 12.dp,
  val large: Dp = 16.dp,
  val extraLarge: Dp = 24.dp,
  val huge: Dp = 32.dp,
  val gutter: Dp = 20.dp
)

internal val LocalMugshotSpacing = staticCompositionLocalOf { MugshotSpacing() }

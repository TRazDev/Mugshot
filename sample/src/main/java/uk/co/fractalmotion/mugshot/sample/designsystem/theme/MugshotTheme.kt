package uk.co.fractalmotion.mugshot.sample.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * The sample's theme.
 *
 * [darkTheme] defaults to [isSystemInDarkTheme], which under Mugshot is driven by
 * `DeviceConfig.copy(nightMode = NightMode.NIGHT)` — that is what the dark mode snapshot test
 * flips, rather than passing a flag in by hand.
 */
@Composable
internal fun MugshotTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalMugshotSpacing provides MugshotSpacing()) {
    MaterialTheme(
      colorScheme = if (darkTheme) MugshotDarkColorScheme else MugshotLightColorScheme,
      typography = MugshotTypography,
      shapes = MugshotShapes,
      content = content
    )
  }
}

/** Shorthand for the spacing scale, mirroring how `MaterialTheme.colorScheme` reads. */
internal val MaterialTheme.spacing: MugshotSpacing
  @Composable get() = LocalMugshotSpacing.current

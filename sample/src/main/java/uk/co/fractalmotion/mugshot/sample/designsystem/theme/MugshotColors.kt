package uk.co.fractalmotion.mugshot.sample.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * A violet/teal/amber palette, hand-tuned rather than generated, so the sample reads as a real
 * product rather than as Material defaults.
 */
internal object MugshotPalette {
  val Violet10 = Color(0xFF170065)
  val Violet20 = Color(0xFF2A1799)
  val Violet40 = Color(0xFF4232C7)
  val Violet50 = Color(0xFF5B4BE0)
  val Violet80 = Color(0xFFC4C0FF)
  val Violet90 = Color(0xFFE4E0FF)

  val Teal10 = Color(0xFF002022)
  val Teal20 = Color(0xFF003739)
  val Teal30 = Color(0xFF004F53)
  val Teal50 = Color(0xFF00A0A8)
  val Teal70 = Color(0xFF4FD8E0)
  val Teal90 = Color(0xFFB2F2F6)

  val Amber10 = Color(0xFF2B1700)
  val Amber20 = Color(0xFF482900)
  val Amber30 = Color(0xFF663D00)
  val Amber60 = Color(0xFFF2A03D)
  val Amber80 = Color(0xFFFFB870)
  val Amber90 = Color(0xFFFFDDB5)

  val Red10 = Color(0xFF410002)
  val Red20 = Color(0xFF690005)
  val Red30 = Color(0xFF93000A)
  val Red40 = Color(0xFFBA1A1A)
  val Red80 = Color(0xFFFFB4AB)
  val Red90 = Color(0xFFFFDAD6)

  val Neutral4 = Color(0xFF0E0E13)
  val Neutral6 = Color(0xFF131318)
  val Neutral10 = Color(0xFF1B1B21)
  val Neutral12 = Color(0xFF1F1F25)
  val Neutral17 = Color(0xFF2A2930)
  val Neutral20 = Color(0xFF303036)
  val Neutral22 = Color(0xFF35343B)
  val Neutral30 = Color(0xFF47464F)
  val Neutral60 = Color(0xFF787680)
  val Neutral70 = Color(0xFF918F9A)
  val Neutral80 = Color(0xFFC8C5D0)
  val Neutral90 = Color(0xFFE4E1E9)
  val Neutral92 = Color(0xFFEAE7EF)
  val Neutral94 = Color(0xFFEFEDF4)
  val Neutral96 = Color(0xFFF5F2FA)
  val Neutral98 = Color(0xFFFBF8FF)
  val NeutralVariant90 = Color(0xFFE4E1EC)
  val White = Color(0xFFFFFFFF)
  val Black = Color(0xFF000000)
}

internal val MugshotLightColorScheme: ColorScheme = lightColorScheme(
  primary = MugshotPalette.Violet50,
  onPrimary = MugshotPalette.White,
  primaryContainer = MugshotPalette.Violet90,
  onPrimaryContainer = MugshotPalette.Violet10,
  inversePrimary = MugshotPalette.Violet80,
  secondary = MugshotPalette.Teal50,
  onSecondary = MugshotPalette.White,
  secondaryContainer = MugshotPalette.Teal90,
  onSecondaryContainer = MugshotPalette.Teal10,
  tertiary = MugshotPalette.Amber60,
  onTertiary = MugshotPalette.White,
  tertiaryContainer = MugshotPalette.Amber90,
  onTertiaryContainer = MugshotPalette.Amber10,
  error = MugshotPalette.Red40,
  onError = MugshotPalette.White,
  errorContainer = MugshotPalette.Red90,
  onErrorContainer = MugshotPalette.Red10,
  background = MugshotPalette.Neutral98,
  onBackground = MugshotPalette.Neutral10,
  surface = MugshotPalette.Neutral98,
  onSurface = MugshotPalette.Neutral10,
  surfaceVariant = MugshotPalette.NeutralVariant90,
  onSurfaceVariant = MugshotPalette.Neutral30,
  surfaceContainerLowest = MugshotPalette.White,
  surfaceContainerLow = MugshotPalette.Neutral96,
  surfaceContainer = MugshotPalette.Neutral94,
  surfaceContainerHigh = MugshotPalette.Neutral92,
  surfaceContainerHighest = MugshotPalette.Neutral90,
  inverseSurface = MugshotPalette.Neutral20,
  inverseOnSurface = MugshotPalette.Neutral96,
  outline = MugshotPalette.Neutral60,
  outlineVariant = MugshotPalette.Neutral80,
  scrim = MugshotPalette.Black
)

internal val MugshotDarkColorScheme: ColorScheme = darkColorScheme(
  primary = MugshotPalette.Violet80,
  onPrimary = MugshotPalette.Violet20,
  primaryContainer = MugshotPalette.Violet40,
  onPrimaryContainer = MugshotPalette.Violet90,
  inversePrimary = MugshotPalette.Violet50,
  secondary = MugshotPalette.Teal70,
  onSecondary = MugshotPalette.Teal20,
  secondaryContainer = MugshotPalette.Teal30,
  onSecondaryContainer = MugshotPalette.Teal90,
  tertiary = MugshotPalette.Amber80,
  onTertiary = MugshotPalette.Amber20,
  tertiaryContainer = MugshotPalette.Amber30,
  onTertiaryContainer = MugshotPalette.Amber90,
  error = MugshotPalette.Red80,
  onError = MugshotPalette.Red20,
  errorContainer = MugshotPalette.Red30,
  onErrorContainer = MugshotPalette.Red90,
  background = MugshotPalette.Neutral6,
  onBackground = MugshotPalette.Neutral90,
  surface = MugshotPalette.Neutral6,
  onSurface = MugshotPalette.Neutral90,
  surfaceVariant = MugshotPalette.Neutral30,
  onSurfaceVariant = MugshotPalette.Neutral80,
  surfaceContainerLowest = MugshotPalette.Neutral4,
  surfaceContainerLow = MugshotPalette.Neutral10,
  surfaceContainer = MugshotPalette.Neutral12,
  surfaceContainerHigh = MugshotPalette.Neutral17,
  surfaceContainerHighest = MugshotPalette.Neutral22,
  inverseSurface = MugshotPalette.Neutral90,
  inverseOnSurface = MugshotPalette.Neutral20,
  outline = MugshotPalette.Neutral70,
  outlineVariant = MugshotPalette.Neutral30,
  scrim = MugshotPalette.Black
)

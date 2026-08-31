package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.annotations.MugshotLightDark

/**
 * Reads [isSystemInDarkTheme] so the two goldens differ.
 *
 * @MugshotLightDark puts the renderer into night configuration rather than forcing a theme from
 * outside, so a composable that ignores the system setting would render identically both times.
 */
@Mugshot
@MugshotLightDark
@Preview
@Composable
internal fun HelloPreview() {
  val dark = isSystemInDarkTheme()
  Surface(color = if (dark) Color.Black else Color.White) {
    Text(text = "Hello", color = if (dark) Color.White else Color.Black)
  }
}

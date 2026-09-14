package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot

/**
 * One preview is enough: what this fixture guards is the generated-test task existing at all, not
 * how many cases it holds.
 */
@Mugshot
@Preview
@Composable
internal fun HelloPreview() {
  Surface(color = Color.White) {
    Text(text = "Hello", color = Color.Black)
  }
}

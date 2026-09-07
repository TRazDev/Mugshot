package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot

@Mugshot
@Preview
@Composable
internal fun HelloPreview() {
  Surface {
    Text(text = "Hello")
  }
}

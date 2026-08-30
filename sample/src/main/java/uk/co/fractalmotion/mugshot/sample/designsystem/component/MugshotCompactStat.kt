package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A single figure sized for a watch face.
 *
 * Exists so the wear golden shows something designed for a round screen rather than a phone layout
 * squeezed into one. Fills the viewport, because on a round device Mugshot clips the frame to a
 * circle and anything short of full bleed leaves a crescent of window background.
 */
@Composable
internal fun MugshotCompactStat(progress: Float, label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(contentAlignment = Alignment.Center) {
      MugshotProgressRing(
        progress = progress,
        label = label,
        value = value,
        size = 148.dp,
        strokeWidth = 14.dp
      )
    }
  }
}

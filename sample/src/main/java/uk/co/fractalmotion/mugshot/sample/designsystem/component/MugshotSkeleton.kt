package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * A loading placeholder.
 *
 * Deliberately static rather than a shimmer: an animated placeholder would render at whatever frame
 * the snapshot happened to catch, which is not something a golden can hold stable.
 */
@Composable
internal fun MugshotSkeletonBlock(modifier: Modifier = Modifier, height: Dp = 16.dp, widthFraction: Float = 1f) {
  Box(
    modifier = modifier
      .fillMaxWidth(widthFraction)
      .height(height)
      .clip(MaterialTheme.shapes.small)
      .background(MaterialTheme.colorScheme.surfaceContainerHighest)
  )
}

/** A placeholder standing in for a card of content. */
@Composable
internal fun MugshotSkeletonCard(modifier: Modifier = Modifier) {
  MugshotCard(modifier = modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
      MugshotSkeletonBlock(height = 96.dp)
      MugshotSkeletonBlock(height = 14.dp, widthFraction = 0.7f)
      MugshotSkeletonBlock(height = 12.dp, widthFraction = 0.45f)
    }
  }
}

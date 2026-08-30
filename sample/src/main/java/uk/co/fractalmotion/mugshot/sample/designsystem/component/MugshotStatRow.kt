package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * Three or four summary figures sharing one card.
 *
 * Separate [MugshotStatTile]s each own their own width, so a long value wraps and leaves the row
 * ragged. Sharing a card lets the figures size down together and keeps their baselines aligned.
 */
@Composable
internal fun MugshotStatRow(stats: List<Pair<String, String>>, modifier: Modifier = Modifier) {
  MugshotCard(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
      stats.forEach { (value, label) ->
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
          )
        }
      }
    }
  }
}

package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * A title/subtitle row with optional leading and trailing slots.
 *
 * Slot based rather than parameterised on icons so a caller can put artwork, an avatar or a switch
 * in either end without the row growing another parameter each time.
 */
@Composable
internal fun MugshotListRow(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  leading: (@Composable () -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = MaterialTheme.spacing.small),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
  ) {
    if (leading != null) {
      Box { leading() }
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
    if (trailing != null) {
      Box { trailing() }
    }
  }
}

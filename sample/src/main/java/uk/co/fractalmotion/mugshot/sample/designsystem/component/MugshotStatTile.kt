package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/** A single headline number with its label, used across the health, gym and smart home screens. */
@Composable
internal fun MugshotStatTile(
  value: String,
  label: String,
  modifier: Modifier = Modifier,
  @DrawableRes icon: Int? = null,
  accent: Color = MaterialTheme.colorScheme.primary,
  supporting: String? = null
) {
  MugshotCard(modifier = modifier, contentPadding = MaterialTheme.spacing.large) {
    if (icon != null) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = accent,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    }
    Text(
      text = value,
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (supporting != null) {
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
      Text(text = supporting, style = MaterialTheme.typography.labelMedium, color = accent)
    }
  }
}

/** A compact horizontal stat, for rows where a full tile would be too heavy. */
@Composable
internal fun MugshotInlineStat(
  value: String,
  label: String,
  modifier: Modifier = Modifier,
  accent: Color = MaterialTheme.colorScheme.primary
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
  ) {
    Text(text = value, style = MaterialTheme.typography.titleLarge, color = accent)
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

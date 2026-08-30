package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/** Shown when a list has nothing in it — the state most design systems forget to draw. */
@Composable
internal fun MugshotEmptyState(
  @DrawableRes icon: Int,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  action: (@Composable () -> Unit)? = null
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = MaterialTheme.spacing.huge),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(MaterialTheme.shapes.extraLarge)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = MaterialTheme.spacing.huge)
    )
    if (action != null) {
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
      action()
    }
  }
}

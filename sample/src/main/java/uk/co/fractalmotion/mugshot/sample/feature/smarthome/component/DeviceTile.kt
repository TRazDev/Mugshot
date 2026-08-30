package uk.co.fractalmotion.mugshot.sample.feature.smarthome.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtwork
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing
import uk.co.fractalmotion.mugshot.sample.feature.smarthome.SmartDevice

@Composable
internal fun DeviceTile(device: SmartDevice, modifier: Modifier = Modifier) {
  MugshotCard(modifier = modifier, contentPadding = MaterialTheme.spacing.medium) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      MugshotArtwork(
        icon = device.icon,
        contentDescription = null,
        paletteIndex = device.paletteIndex,
        glyphSize = 22.dp,
        modifier = Modifier.size(44.dp)
      )
      Switch(checked = device.on, onCheckedChange = {})
    }
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
    Text(
      text = stringResource(device.nameRes),
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = device.detail,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

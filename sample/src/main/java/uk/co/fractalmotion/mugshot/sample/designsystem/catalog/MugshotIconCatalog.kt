package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtwork
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotAvatar
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

private val CatalogIcons = listOf(
  R.drawable.ic_brand_mark to "brand",
  R.drawable.ic_sneaker to "sneaker",
  R.drawable.ic_headphones to "headphones",
  R.drawable.ic_watch to "watch",
  R.drawable.ic_backpack to "backpack",
  R.drawable.ic_thermostat to "thermostat",
  R.drawable.ic_lightbulb to "lightbulb",
  R.drawable.ic_speaker to "speaker",
  R.drawable.ic_dumbbell to "dumbbell",
  R.drawable.ic_heart_pulse to "heart",
  R.drawable.ic_footsteps to "steps",
  R.drawable.ic_sleep to "sleep",
  R.drawable.ic_water_drop to "water",
  R.drawable.ic_flame to "flame"
)

@Composable
internal fun MugshotIconCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Iconography", modifier = modifier) {
    CatalogIcons.chunked(4).forEach { chunk ->
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
        chunk.forEach { (icon, label) ->
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
          ) {
            Icon(
              painter = painterResource(id = icon),
              contentDescription = label,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(28.dp)
            )
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        repeat(4 - chunk.size) {
          Column(modifier = Modifier.weight(1f)) {}
        }
      }
    }
    MugshotCatalogRow(label = "Artwork") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotArtwork(
          icon = R.drawable.ic_sneaker,
          contentDescription = null,
          paletteIndex = 0,
          modifier = Modifier.size(72.dp)
        )
        MugshotArtwork(
          icon = R.drawable.ic_headphones,
          contentDescription = null,
          paletteIndex = 1,
          modifier = Modifier.size(72.dp)
        )
        MugshotArtwork(
          icon = R.drawable.ic_watch,
          contentDescription = null,
          paletteIndex = 2,
          modifier = Modifier.size(72.dp)
        )
      }
    }
    MugshotCatalogRow(label = "Avatars") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotAvatar(initials = "AR", paletteIndex = 0)
        MugshotAvatar(initials = "JD", paletteIndex = 1)
        MugshotAvatar(initials = "MK", paletteIndex = 2)
      }
    }
  }
}

@Preview
@Composable
internal fun MugshotIconCatalogPreview() {
  MugshotTheme { MugshotIconCatalog() }
}

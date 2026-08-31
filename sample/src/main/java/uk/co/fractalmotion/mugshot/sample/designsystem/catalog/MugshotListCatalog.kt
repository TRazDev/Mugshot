package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.annotations.MugshotLightDark
import uk.co.fractalmotion.mugshot.annotations.MugshotShrink
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotEmptyState
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotListRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatusChip
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTextButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTonalButton
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtwork
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotAvatar
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

@Composable
internal fun MugshotListCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Lists and states", modifier = modifier) {
    MugshotCatalogRow(label = "Section header with action") {
      MugshotSectionHeader(
        title = "Recent orders",
        subtitle = "Last 30 days",
        action = { MugshotTextButton(text = "See all", onClick = {}) }
      )
    }
    MugshotCatalogRow(label = "Rows with leading and trailing slots") {
      MugshotListRow(
        title = "Trail Runner GTX",
        subtitle = "Size 9 · Graphite",
        leading = {
          MugshotArtwork(
            icon = R.drawable.ic_sneaker,
            contentDescription = null,
            paletteIndex = 0,
            glyphSize = 24.dp,
            modifier = Modifier.size(48.dp)
          )
        },
        trailing = { Text(text = "£129", style = MaterialTheme.typography.titleSmall) }
      )
      MugshotListRow(
        title = "Ada Rivera",
        subtitle = "Training partner",
        leading = { MugshotAvatar(initials = "AR", size = 48.dp, paletteIndex = 1) },
        trailing = { MugshotStatusChip(text = "Online") }
      )
      MugshotListRow(
        title = "Hallway lamp",
        subtitle = "Warm white · 40%",
        leading = {
          MugshotArtwork(
            icon = R.drawable.ic_lightbulb,
            contentDescription = null,
            paletteIndex = 2,
            glyphSize = 24.dp,
            modifier = Modifier.size(48.dp)
          )
        },
        trailing = { Switch(checked = true, onCheckedChange = {}) }
      )
    }
    MugshotCatalogRow(label = "Empty state") {
      MugshotEmptyState(
        icon = R.drawable.ic_backpack,
        title = "Your bag is empty",
        description = "Items you add will show up here, ready to check out.",
        action = { MugshotTonalButton(text = "Browse the shop", onClick = {}) }
      )
    }
  }
}

@Mugshot
@MugshotShrink
@MugshotLightDark
@Preview
@Composable
internal fun MugshotListCatalogPreview() {
  MugshotTheme { MugshotListCatalog() }
}

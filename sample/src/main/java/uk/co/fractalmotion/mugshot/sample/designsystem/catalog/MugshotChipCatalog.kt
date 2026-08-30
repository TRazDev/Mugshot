package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotAssistChip
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotFilterChipRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatusChip
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun MugshotChipCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Chips", modifier = modifier) {
    MugshotCatalogRow(label = "Filter rail, second option selected") {
      MugshotFilterChipRow(
        options = listOf("All", "Shoes", "Audio"),
        selectedIndex = 1,
        onSelect = {}
      )
    }
    MugshotCatalogRow(label = "Status") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotStatusChip(text = "In stock")
        MugshotStatusChip(
          text = "Low stock",
          container = MaterialTheme.colorScheme.tertiaryContainer,
          content = MaterialTheme.colorScheme.onTertiaryContainer
        )
        MugshotStatusChip(
          text = "Sold out",
          container = MaterialTheme.colorScheme.errorContainer,
          content = MaterialTheme.colorScheme.onErrorContainer
        )
      }
    }
    MugshotCatalogRow(label = "Assist") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotAssistChip(text = "Free delivery", onClick = {})
        MugshotAssistChip(text = "2 year warranty", onClick = {})
      }
    }
  }
}

@Preview
@Composable
internal fun MugshotChipCatalogPreview() {
  MugshotTheme { MugshotChipCatalog() }
}

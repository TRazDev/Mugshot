package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotHeroCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatTile
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotElevation
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun MugshotCardCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Cards and surfaces", modifier = modifier) {
    MugshotCatalogRow(label = "Hero") {
      MugshotHeroCard {
        Text(
          text = "Good morning, Ada",
          style = MaterialTheme.typography.titleLarge,
          color = Color.White
        )
        Text(
          text = "Three sessions left this week",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.85f)
        )
      }
    }
    MugshotCatalogRow(label = "Container at tonal levels 1 and 3") {
      MugshotCard(modifier = Modifier.fillMaxWidth(), tonalElevation = MugshotElevation.level1) {
        Text(
          text = "Tonal level 1",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Tonal elevation is a flat colour shift, so it renders identically everywhere.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
      MugshotCard(modifier = Modifier.fillMaxWidth(), tonalElevation = MugshotElevation.level3) {
        Text(
          text = "Tonal level 3",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
    MugshotCatalogRow(label = "Stat tile") {
      MugshotStatTile(
        value = "8,420",
        label = "Steps today",
        icon = R.drawable.ic_footsteps,
        supporting = "+12% on yesterday",
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Preview
@Composable
internal fun MugshotCardCatalogPreview() {
  MugshotTheme { MugshotCardCatalog() }
}

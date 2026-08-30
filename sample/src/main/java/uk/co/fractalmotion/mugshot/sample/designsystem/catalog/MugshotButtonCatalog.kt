package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.annotations.MugshotLightDark
import uk.co.fractalmotion.mugshot.annotations.MugshotShrink
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotOutlinedButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotPrimaryButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTextButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTonalButton
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun MugshotButtonCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Buttons", modifier = modifier) {
    MugshotCatalogRow(label = "Emphasis") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotPrimaryButton(text = "Primary", onClick = {})
        MugshotTonalButton(text = "Tonal", onClick = {})
      }
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotOutlinedButton(text = "Outlined", onClick = {})
        MugshotTextButton(text = "Text", onClick = {})
      }
    }
    MugshotCatalogRow(label = "With icon") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotPrimaryButton(text = "Add item", onClick = {}, icon = Icons.Filled.Add)
        MugshotTonalButton(text = "Add", onClick = {}, icon = Icons.Filled.Add)
      }
    }
    MugshotCatalogRow(label = "Disabled") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotPrimaryButton(text = "Primary", onClick = {}, enabled = false)
        MugshotTonalButton(text = "Tonal", onClick = {}, enabled = false)
        MugshotOutlinedButton(text = "Outlined", onClick = {}, enabled = false)
      }
    }
  }
}

@Mugshot
@MugshotShrink
@MugshotLightDark
@Preview
@Composable
internal fun MugshotButtonCatalogPreview() {
  MugshotTheme { MugshotButtonCatalog() }
}

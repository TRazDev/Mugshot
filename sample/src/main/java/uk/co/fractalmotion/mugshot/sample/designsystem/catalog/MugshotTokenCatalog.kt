package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.annotations.MugshotLightDark
import uk.co.fractalmotion.mugshot.annotations.MugshotShrink
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun MugshotTokenCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Colour and type tokens", modifier = modifier) {
    val scheme = MaterialTheme.colorScheme
    MugshotCatalogRow(label = "Accents") {
      SwatchRow(
        swatches = listOf(
          Triple("primary", scheme.primary, scheme.onPrimary),
          Triple("secondary", scheme.secondary, scheme.onSecondary),
          Triple("tertiary", scheme.tertiary, scheme.onTertiary),
          Triple("error", scheme.error, scheme.onError)
        )
      )
    }
    MugshotCatalogRow(label = "Containers") {
      SwatchRow(
        swatches = listOf(
          Triple("primary", scheme.primaryContainer, scheme.onPrimaryContainer),
          Triple("secondary", scheme.secondaryContainer, scheme.onSecondaryContainer),
          Triple("tertiary", scheme.tertiaryContainer, scheme.onTertiaryContainer),
          Triple("error", scheme.errorContainer, scheme.onErrorContainer)
        )
      )
    }
    MugshotCatalogRow(label = "Surfaces") {
      SwatchRow(
        swatches = listOf(
          Triple("lowest", scheme.surfaceContainerLowest, scheme.onSurface),
          Triple("low", scheme.surfaceContainerLow, scheme.onSurface),
          Triple("base", scheme.surfaceContainer, scheme.onSurface),
          Triple("highest", scheme.surfaceContainerHighest, scheme.onSurface)
        )
      )
    }
    MugshotCatalogRow(label = "Type scale") {
      Text(text = "Display small", style = MaterialTheme.typography.displaySmall)
      Text(text = "Headline medium", style = MaterialTheme.typography.headlineMedium)
      Text(text = "Title large", style = MaterialTheme.typography.titleLarge)
      Text(text = "Body medium — the workhorse", style = MaterialTheme.typography.bodyMedium)
      Text(text = "LABEL SMALL", style = MaterialTheme.typography.labelSmall)
    }
  }
}

@Composable
private fun SwatchRow(swatches: List<Triple<String, Color, Color>>, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
  ) {
    swatches.forEach { (label, container, content) ->
      Column(modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .padding(MaterialTheme.spacing.extraSmall),
          contentAlignment = Alignment.BottomStart
        ) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            textAlign = TextAlign.Start
          )
        }
      }
    }
  }
}

@Mugshot
@MugshotShrink
@MugshotLightDark
@Preview
@Composable
internal fun MugshotTokenCatalogPreview() {
  MugshotTheme { MugshotTokenCatalog() }
}

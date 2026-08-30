package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * The frame every catalog page sits in.
 *
 * Catalog pages are snapshotted with `RenderingMode.SHRINK`, so they wrap to their content height
 * rather than filling a device. The fixed width keeps every gallery golden the same size regardless
 * of how wide its widest child happens to be.
 */
@Composable
internal fun MugshotCatalogPage(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier.width(360.dp),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier.padding(MaterialTheme.spacing.gutter),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
      )
      content()
    }
  }
}

/** A labelled specimen within a catalog page. */
@Composable
internal fun MugshotCatalogRow(
  label: String,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    content()
  }
}

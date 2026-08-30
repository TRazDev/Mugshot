package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/** A read only state marker: in stock, on track, overdue. */
@Composable
internal fun MugshotStatusChip(
  text: String,
  modifier: Modifier = Modifier,
  container: Color = MaterialTheme.colorScheme.secondaryContainer,
  content: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraLarge,
    color = container
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = content,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

/** A single selection filter rail. */
@Composable
internal fun MugshotFilterChipRow(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
  ) {
    options.forEachIndexed { index, option ->
      FilterChip(
        selected = index == selectedIndex,
        onClick = { onSelect(index) },
        label = { Text(text = option, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primary,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    }
  }
}

/** A non interactive informational chip. */
@Composable
internal fun MugshotAssistChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  AssistChip(
    onClick = onClick,
    label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
    modifier = modifier,
    shape = MaterialTheme.shapes.extraLarge,
    colors = AssistChipDefaults.assistChipColors()
  )
}

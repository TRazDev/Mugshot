package uk.co.fractalmotion.mugshot.sample.feature.smarthome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotBarChart
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotFilterChipRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotHeroCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing
import uk.co.fractalmotion.mugshot.sample.feature.smarthome.component.DeviceTile

@Composable
internal fun SmartHomeScreen(state: SmartHomeUiState, modifier: Modifier = Modifier) {
  MugshotScreenScaffold(title = stringResource(R.string.smarthome_title), modifier = modifier) {
    MugshotHeroCard {
      Text(
        text = stringResource(R.string.smarthome_greeting),
        style = MaterialTheme.typography.headlineSmall,
        color = Color.White
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
      Text(
        text = stringResource(R.string.smarthome_summary),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f)
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = "${state.temperatureCelsius}°",
          style = MaterialTheme.typography.displayMedium,
          color = Color.White
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.smarthome_temperature_label),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.85f)
          )
          Text(
            text = "→ ${state.targetCelsius}°",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f)
          )
        }
      }
    }

    MugshotFilterChipRow(
      options = state.rooms.map { stringResource(it) },
      selectedIndex = state.selectedRoom,
      onSelect = {}
    )

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      MugshotSectionHeader(title = stringResource(R.string.smarthome_section_devices))
      state.devices.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
          row.forEach { device ->
            DeviceTile(device = device, modifier = Modifier.weight(1f))
          }
          repeat(2 - row.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      MugshotSectionHeader(title = stringResource(R.string.smarthome_section_energy))
      MugshotCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = stringResource(R.string.smarthome_energy_total),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = stringResource(R.string.smarthome_energy_detail, state.energySavingPercent),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        MugshotBarChart(
          values = state.energyByDay,
          labels = state.energyDayLabels,
          accent = MaterialTheme.colorScheme.secondary,
          highlightIndex = 3
        )
      }
    }
  }
}

@Mugshot
@Preview
@Composable
internal fun SmartHomeScreenPreview() {
  MugshotTheme { SmartHomeScreen(state = SmartHomeFixtures.sample) }
}

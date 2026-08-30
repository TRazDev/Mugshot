package uk.co.fractalmotion.mugshot.sample.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotFilterChipRow
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing
import uk.co.fractalmotion.mugshot.sample.feature.storefront.ProductStatus
import uk.co.fractalmotion.mugshot.sample.feature.storefront.StorefrontFixtures
import uk.co.fractalmotion.mugshot.sample.feature.storefront.component.ProductCard

/**
 * Every state of a component in one golden.
 *
 * The gallery test shows components as they are normally used; this one lines a single component up
 * in all of its states side by side, which is where an unstyled disabled colour or an unreadable
 * selected label shows up immediately.
 */
class ComponentStateSnapshotTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.PIXEL_6,
    renderingMode = RenderingMode.SHRINK
  )

  @Test
  fun productCardStatuses() {
    mugshot.snapshot {
      MugshotTheme {
        Row(
          modifier = Modifier.width(560.dp),
          horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
          ProductStatus.entries.forEach { status ->
            ProductCard(
              product = StorefrontFixtures.trailRunner.copy(status = status),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }

  @Test
  fun filterChipSelection() {
    mugshot.snapshot {
      MugshotTheme {
        Column(
          modifier = Modifier.width(360.dp),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
          val options = listOf("All", "Shoes", "Audio")
          options.indices.forEach { selected ->
            MugshotFilterChipRow(
              options = options,
              selectedIndex = selected,
              onSelect = {}
            )
          }
        }
      }
    }
  }
}

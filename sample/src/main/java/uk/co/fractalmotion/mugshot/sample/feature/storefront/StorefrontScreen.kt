package uk.co.fractalmotion.mugshot.sample.feature.storefront

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotEmptyState
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotFilterChipRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotHeroCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSkeletonCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTextButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTonalButton
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing
import uk.co.fractalmotion.mugshot.sample.feature.storefront.component.ProductCard

@Composable
internal fun StorefrontScreen(state: StorefrontUiState, modifier: Modifier = Modifier) {
  MugshotScreenScaffold(title = stringResource(R.string.storefront_title), modifier = modifier) {
    MugshotFilterChipRow(
      options = state.categories.map { stringResource(it) },
      selectedIndex = state.selectedCategory,
      onSelect = {}
    )

    MugshotHeroCard {
      Text(
        text = stringResource(R.string.storefront_promo_headline),
        style = MaterialTheme.typography.headlineSmall,
        color = Color.White
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
      Text(
        text = stringResource(R.string.storefront_promo_detail, state.promoDiscountPercent),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f)
      )
    }

    MugshotSectionHeader(
      title = stringResource(R.string.storefront_section_featured),
      action = { MugshotTextButton(text = stringResource(R.string.action_see_all), onClick = {}) }
    )

    when {
      state.loading -> Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
      ) {
        repeat(2) {
          Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            MugshotSkeletonCard(modifier = Modifier.weight(1f))
            MugshotSkeletonCard(modifier = Modifier.weight(1f))
          }
        }
      }

      state.products.isEmpty() -> MugshotEmptyState(
        icon = R.drawable.ic_backpack,
        title = stringResource(R.string.storefront_empty_title),
        description = stringResource(R.string.storefront_empty_detail),
        action = {
          MugshotTonalButton(text = stringResource(R.string.action_browse_shop), onClick = {})
        }
      )

      else -> Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
      ) {
        state.products.chunked(2).forEach { row ->
          Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            row.forEach { product ->
              ProductCard(product = product, modifier = Modifier.weight(1f))
            }
            repeat(2 - row.size) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

/**
 * The three states the storefront has to draw.
 *
 * One `@PreviewParameter` preview rather than three separate previews: the processor expands the
 * provider at test runtime into one `mugshotPreviews` entry per value, so all three states are
 * snapshotted from a single declaration.
 */
internal class StorefrontStateProvider : PreviewParameterProvider<StorefrontUiState> {
  override val values = sequenceOf(
    StorefrontFixtures.populated,
    StorefrontFixtures.empty,
    StorefrontFixtures.loading
  )
}

@Mugshot
@Preview
@Composable
internal fun StorefrontScreenPreview(@PreviewParameter(StorefrontStateProvider::class) state: StorefrontUiState) {
  MugshotTheme { StorefrontScreen(state = state) }
}

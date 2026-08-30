package uk.co.fractalmotion.mugshot.sample.feature.storefront

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotAssistChip
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotPrimaryButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtworkBanner
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing
import uk.co.fractalmotion.mugshot.sample.feature.storefront.component.ProductStatusChip

@Composable
internal fun ProductDetailScreen(product: Product, modifier: Modifier = Modifier) {
  MugshotScreenScaffold(
    title = stringResource(R.string.storefront_detail_title),
    modifier = modifier,
    onBack = {}
  ) {
    MugshotArtworkBanner(
      icon = product.icon,
      contentDescription = stringResource(product.nameRes),
      paletteIndex = product.paletteIndex
    )

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
      Text(
        text = stringResource(product.nameRes),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = stringResource(product.detailRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
      ) {
        Text(
          text = product.price,
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.primary
        )
        ProductStatusChip(status = product.status)
      }
      Text(
        text = stringResource(R.string.storefront_rating, product.rating, product.reviewCount),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
      MugshotAssistChip(text = stringResource(R.string.storefront_detail_free_delivery), onClick = {})
      MugshotAssistChip(text = stringResource(R.string.storefront_detail_warranty), onClick = {})
    }

    Text(
      text = stringResource(R.string.storefront_detail_description),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    MugshotPrimaryButton(
      text = stringResource(R.string.action_add_to_bag),
      onClick = {},
      enabled = product.status != ProductStatus.SOLD_OUT,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Mugshot
@Preview
@Composable
internal fun ProductDetailScreenPreview() {
  MugshotTheme { ProductDetailScreen(product = StorefrontFixtures.trailRunner) }
}

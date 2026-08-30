package uk.co.fractalmotion.mugshot.sample.feature.storefront.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatusChip
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtwork
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing
import uk.co.fractalmotion.mugshot.sample.feature.storefront.Product
import uk.co.fractalmotion.mugshot.sample.feature.storefront.ProductStatus

@Composable
internal fun ProductCard(product: Product, modifier: Modifier = Modifier) {
  MugshotCard(modifier = modifier, contentPadding = MaterialTheme.spacing.medium) {
    MugshotArtwork(
      icon = product.icon,
      contentDescription = null,
      paletteIndex = product.paletteIndex,
      glyphSize = 40.dp,
      modifier = Modifier
        .fillMaxWidth()
        .height(112.dp)
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
    Text(
      text = stringResource(product.nameRes),
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = stringResource(product.detailRes),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = product.price,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      ProductStatusChip(status = product.status)
    }
  }
}

@Composable
internal fun ProductStatusChip(status: ProductStatus, modifier: Modifier = Modifier) {
  when (status) {
    ProductStatus.IN_STOCK -> MugshotStatusChip(
      text = stringResource(R.string.storefront_status_in_stock),
      modifier = modifier
    )

    ProductStatus.LOW_STOCK -> MugshotStatusChip(
      text = stringResource(R.string.storefront_status_low_stock),
      modifier = modifier,
      container = MaterialTheme.colorScheme.tertiaryContainer,
      content = MaterialTheme.colorScheme.onTertiaryContainer
    )

    ProductStatus.SOLD_OUT -> MugshotStatusChip(
      text = stringResource(R.string.storefront_status_sold_out),
      modifier = modifier,
      container = MaterialTheme.colorScheme.errorContainer,
      content = MaterialTheme.colorScheme.onErrorContainer
    )
  }
}

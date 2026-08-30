package uk.co.fractalmotion.mugshot.sample.feature.storefront

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Storefront state, including the empty and loading cases the screen has to draw. */
internal data class StorefrontUiState(
  @StringRes val categories: List<Int>,
  val selectedCategory: Int,
  val promoDiscountPercent: Int,
  val products: List<Product>,
  val loading: Boolean = false
)

internal data class Product(
  @StringRes val nameRes: Int,
  @StringRes val detailRes: Int,
  @DrawableRes val icon: Int,
  val price: String,
  val status: ProductStatus,
  val paletteIndex: Int,
  val rating: String = "4.8",
  val reviewCount: Int = 312
)

internal enum class ProductStatus {
  IN_STOCK,
  LOW_STOCK,
  SOLD_OUT
}

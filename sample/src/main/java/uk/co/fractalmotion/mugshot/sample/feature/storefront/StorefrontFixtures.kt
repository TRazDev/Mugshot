package uk.co.fractalmotion.mugshot.sample.feature.storefront

import uk.co.fractalmotion.mugshot.sample.R

/** Sample content for the storefront, including the states a real shop has to handle. */
internal object StorefrontFixtures {
  private val categories = listOf(
    R.string.storefront_category_all,
    R.string.storefront_category_shoes,
    R.string.storefront_category_audio,
    R.string.storefront_category_wearables
  )

  val trailRunner = Product(
    nameRes = R.string.storefront_product_runner,
    detailRes = R.string.storefront_product_runner_detail,
    icon = R.drawable.ic_sneaker,
    price = "£129",
    status = ProductStatus.IN_STOCK,
    paletteIndex = 0
  )

  private val fieldBuds = Product(
    nameRes = R.string.storefront_product_headphones,
    detailRes = R.string.storefront_product_headphones_detail,
    icon = R.drawable.ic_headphones,
    price = "£179",
    status = ProductStatus.LOW_STOCK,
    paletteIndex = 1,
    rating = "4.6",
    reviewCount = 884
  )

  private val summitWatch = Product(
    nameRes = R.string.storefront_product_watch,
    detailRes = R.string.storefront_product_watch_detail,
    icon = R.drawable.ic_watch,
    price = "£349",
    status = ProductStatus.SOLD_OUT,
    paletteIndex = 2,
    rating = "4.9",
    reviewCount = 156
  )

  private val ridgeline = Product(
    nameRes = R.string.storefront_product_pack,
    detailRes = R.string.storefront_product_pack_detail,
    icon = R.drawable.ic_backpack,
    price = "£94",
    status = ProductStatus.IN_STOCK,
    paletteIndex = 3,
    rating = "4.4",
    reviewCount = 61
  )

  val populated = StorefrontUiState(
    categories = categories,
    selectedCategory = 0,
    promoDiscountPercent = 30,
    products = listOf(trailRunner, fieldBuds, summitWatch, ridgeline)
  )

  val empty = populated.copy(selectedCategory = 3, products = emptyList())

  val loading = populated.copy(products = emptyList(), loading = true)
}

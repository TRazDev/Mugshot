package uk.co.fractalmotion.mugshot.sample.catalog

import androidx.compose.runtime.Composable
import uk.co.fractalmotion.mugshot.sample.feature.gym.GymFixtures
import uk.co.fractalmotion.mugshot.sample.feature.gym.GymScreen
import uk.co.fractalmotion.mugshot.sample.feature.gym.WorkoutDetailScreen
import uk.co.fractalmotion.mugshot.sample.feature.health.HealthFixtures
import uk.co.fractalmotion.mugshot.sample.feature.health.HealthScreen
import uk.co.fractalmotion.mugshot.sample.feature.profile.ProfileFixtures
import uk.co.fractalmotion.mugshot.sample.feature.profile.ProfileScreen
import uk.co.fractalmotion.mugshot.sample.feature.smarthome.SmartHomeFixtures
import uk.co.fractalmotion.mugshot.sample.feature.smarthome.SmartHomeScreen
import uk.co.fractalmotion.mugshot.sample.feature.storefront.ProductDetailScreen
import uk.co.fractalmotion.mugshot.sample.feature.storefront.StorefrontFixtures
import uk.co.fractalmotion.mugshot.sample.feature.storefront.StorefrontScreen

/**
 * The screens the variant matrices render.
 *
 * This restates the seven screens that `mugshotPreviews` already lists, deliberately. Iterating the
 * generated list here instead would mean every new `@Mugshot` preview silently multiplied itself
 * across every matrix below, and `MugshotPreviewData` — a data class holding a lambda — would put a
 * `Function0` identity hash into each golden filename, so the names would change run to run.
 *
 * Entries do not wrap themselves in `MugshotTheme`; the tests supply it, so one declaration serves
 * the light, dark, and forced-appearance cases alike.
 */
enum class Screen(val content: @Composable () -> Unit) {
  PROFILE({ ProfileScreen(state = ProfileFixtures.sample) }),
  STOREFRONT({ StorefrontScreen(state = StorefrontFixtures.populated) }),
  PRODUCT_DETAIL({ ProductDetailScreen(product = StorefrontFixtures.trailRunner) }),
  SMART_HOME({ SmartHomeScreen(state = SmartHomeFixtures.sample) }),
  GYM({ GymScreen(state = GymFixtures.sample) }),
  WORKOUT_DETAIL({ WorkoutDetailScreen(state = GymFixtures.pushDay) }),
  HEALTH({ HealthScreen(state = HealthFixtures.today) })
}

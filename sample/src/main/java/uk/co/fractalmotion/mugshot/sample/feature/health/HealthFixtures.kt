package uk.co.fractalmotion.mugshot.sample.feature.health

import uk.co.fractalmotion.mugshot.sample.R

internal object HealthFixtures {
  val today = HealthUiState(
    rings = listOf(
      ActivityRing(labelRes = R.string.health_ring_move, progress = 0.72f, value = "72%"),
      ActivityRing(labelRes = R.string.health_ring_exercise, progress = 0.45f, value = "45%"),
      ActivityRing(labelRes = R.string.health_ring_stand, progress = 0.93f, value = "93%")
    ),
    metrics = listOf(
      HealthMetric(
        labelRes = R.string.health_metric_steps,
        supportingRes = R.string.health_metric_steps_supporting,
        icon = R.drawable.ic_footsteps,
        value = "8,420",
        supportingArg = 12
      ),
      HealthMetric(
        labelRes = R.string.health_metric_heart,
        supportingRes = R.string.health_metric_heart_supporting,
        icon = R.drawable.ic_heart_pulse,
        value = "54 bpm"
      ),
      HealthMetric(
        labelRes = R.string.health_metric_water,
        supportingRes = R.string.health_metric_water_supporting,
        icon = R.drawable.ic_water_drop,
        value = "1.4 L"
      ),
      HealthMetric(
        labelRes = R.string.health_metric_energy,
        supportingRes = R.string.health_metric_energy_supporting,
        icon = R.drawable.ic_flame,
        value = "612 kcal"
      )
    ),
    sleepStages = listOf(
      SleepStage(labelRes = R.string.health_sleep_deep, fraction = 0.22f),
      SleepStage(labelRes = R.string.health_sleep_core, fraction = 0.48f),
      SleepStage(labelRes = R.string.health_sleep_rem, fraction = 0.24f),
      SleepStage(labelRes = R.string.health_sleep_awake, fraction = 0.06f)
    ),
    sleepHours = 7,
    sleepMinutes = 24,
    restingHeartRateTrend = listOf(58f, 57f, 59f, 56f, 55f, 57f, 54f, 53f, 54f)
  )

  val noData = today.copy(
    rings = today.rings.map { it.copy(progress = 0f, value = "0%") },
    metrics = emptyList(),
    hasData = false
  )
}

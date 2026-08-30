package uk.co.fractalmotion.mugshot.sample.feature.smarthome

import uk.co.fractalmotion.mugshot.sample.R

internal object SmartHomeFixtures {
  val sample = SmartHomeUiState(
    rooms = listOf(
      R.string.smarthome_room_living,
      R.string.smarthome_room_kitchen,
      R.string.smarthome_room_bedroom
    ),
    selectedRoom = 0,
    temperatureCelsius = 21,
    targetCelsius = 22,
    devices = listOf(
      SmartDevice(
        nameRes = R.string.smarthome_device_thermostat,
        icon = R.drawable.ic_thermostat,
        detail = "21° · heating",
        on = true,
        paletteIndex = 2
      ),
      SmartDevice(
        nameRes = R.string.smarthome_device_lamp,
        icon = R.drawable.ic_lightbulb,
        detail = "Warm white · 40%",
        on = true,
        paletteIndex = 0
      ),
      SmartDevice(
        nameRes = R.string.smarthome_device_speaker,
        icon = R.drawable.ic_speaker,
        detail = "Paused",
        on = false,
        paletteIndex = 1
      )
    ),
    energyByDay = listOf(2.4f, 3.1f, 2.0f, 3.6f, 2.8f, 1.9f, 2.6f),
    energyDayLabels = listOf("M", "T", "W", "T", "F", "S", "S"),
    energySavingPercent = 9
  )
}

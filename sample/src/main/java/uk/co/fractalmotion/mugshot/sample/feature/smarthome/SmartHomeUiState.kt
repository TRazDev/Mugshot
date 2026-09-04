package uk.co.fractalmotion.mugshot.sample.feature.smarthome

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class SmartHomeUiState(
  @param:StringRes val rooms: List<Int>,
  val selectedRoom: Int,
  val temperatureCelsius: Int,
  val targetCelsius: Int,
  val devices: List<SmartDevice>,
  val energyByDay: List<Float>,
  val energyDayLabels: List<String>,
  val energySavingPercent: Int
)

internal data class SmartDevice(
  @param:StringRes val nameRes: Int,
  @param:DrawableRes val icon: Int,
  val detail: String,
  val on: Boolean,
  val paletteIndex: Int
)

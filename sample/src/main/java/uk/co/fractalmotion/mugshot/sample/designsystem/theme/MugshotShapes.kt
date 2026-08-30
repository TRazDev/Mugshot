package uk.co.fractalmotion.mugshot.sample.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Rounder than the Material defaults — the single cheapest way to read as a modern product. */
internal val MugshotShapes: Shapes = Shapes(
  extraSmall = RoundedCornerShape(6.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(26.dp),
  extraLarge = RoundedCornerShape(34.dp)
)

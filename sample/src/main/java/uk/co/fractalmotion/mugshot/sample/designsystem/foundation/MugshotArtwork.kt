package uk.co.fractalmotion.mugshot.sample.designsystem.foundation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stands in for a photograph.
 *
 * layoutlib has no network access, so the sample cannot show real imagery. A gradient panel with
 * the item's glyph over it reads as intentional art direction rather than as a missing image, and
 * it renders identically on every platform.
 */
@Composable
internal fun MugshotArtwork(
  @DrawableRes icon: Int,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  paletteIndex: Int = 0,
  glyphSize: Dp = 40.dp,
  shape: Shape = MaterialTheme.shapes.medium
) {
  Box(
    modifier = modifier
      .clip(shape)
      .background(MugshotGradients.forIndex(paletteIndex)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      painter = painterResource(id = icon),
      contentDescription = contentDescription,
      tint = Color.White.copy(alpha = 0.92f),
      modifier = Modifier.size(glyphSize)
    )
  }
}

/** A wide artwork banner for the top of a detail screen. */
@Composable
internal fun MugshotArtworkBanner(
  @DrawableRes icon: Int,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  paletteIndex: Int = 0,
  height: Dp = 200.dp
) {
  MugshotArtwork(
    icon = icon,
    contentDescription = contentDescription,
    paletteIndex = paletteIndex,
    glyphSize = 76.dp,
    shape = MaterialTheme.shapes.large,
    modifier = modifier.height(height)
  )
}

/** Initials over a gradient — the sample's stand-in for a profile photo. */
@Composable
internal fun MugshotAvatar(initials: String, modifier: Modifier = Modifier, size: Dp = 56.dp, paletteIndex: Int = 0) {
  Box(
    modifier = modifier
      .size(size)
      .clip(MaterialTheme.shapes.extraLarge)
      .background(MugshotGradients.forIndex(paletteIndex)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials,
      style = MaterialTheme.typography.titleMedium,
      color = Color.White
    )
  }
}

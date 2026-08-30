package uk.co.fractalmotion.mugshot.sample.catalog

import androidx.compose.runtime.Composable
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotButtonCatalog
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotCardCatalog
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotChipCatalog
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotIconCatalog
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotListCatalog
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotProgressCatalog
import uk.co.fractalmotion.mugshot.sample.designsystem.catalog.MugshotTokenCatalog

/**
 * The design system's gallery pages.
 *
 * Entries deliberately do not wrap themselves in `MugshotTheme` — the test supplies the theme, so
 * the same page can be rendered light and dark from one declaration.
 */
enum class GalleryPage(val content: @Composable () -> Unit) {
  TOKENS({ MugshotTokenCatalog() }),
  BUTTONS({ MugshotButtonCatalog() }),
  CARDS({ MugshotCardCatalog() }),
  CHIPS({ MugshotChipCatalog() }),
  PROGRESS({ MugshotProgressCatalog() }),
  LISTS({ MugshotListCatalog() }),
  ICONS({ MugshotIconCatalog() })
}

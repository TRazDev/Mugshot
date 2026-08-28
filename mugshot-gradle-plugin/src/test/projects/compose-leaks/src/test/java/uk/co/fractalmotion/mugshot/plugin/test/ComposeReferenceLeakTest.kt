package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.ui.platform.ComposeView
import org.junit.AfterClass
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot
import java.lang.ref.WeakReference

class ComposeReferenceLeakTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun cleansUpComposeReferences() {
    composeView = ComposeView(mugshot.context).apply {
      setContent {
        HelloMugshot()
      }

      mugshot.snapshot(this)
    }
  }

  companion object {
    private var composeView: ComposeView? = null

    @AfterClass
    @JvmStatic
    fun teardown() {
      assert(composeView != null)
      val weakComposeView = WeakReference(composeView)

      composeView = null
      System.gc()

      assert(weakComposeView.get() == null)
    }
  }
}

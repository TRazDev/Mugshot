package uk.co.fractalmotion.mugshot.plugin.test

import android.os.Build
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Touches the `android.*` classes Robolectric instruments, rather than asserting nothing.
 *
 * A no-op body passes even when layoutlib has already claimed those classes in this JVM, which is
 * why the plain `robolectric` fixture never caught them coexisting.
 */
@RunWith(RobolectricTestRunner::class)
class RobolectricTest {
  @Test
  fun readsInstrumentedAndroidClasses() {
    assertTrue(Build.VERSION.SDK_INT > 0)
  }
}

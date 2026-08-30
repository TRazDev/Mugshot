package uk.co.fractalmotion.mugshot.plugin.test

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The plugin forwards every `uk.co.fractalmotion.mugshot.*` Gradle property into the test JVM as a
 * system property. SnapshotVerifier.determineDiffer() reads `...mugshot.differ` from there, so this
 * asserts the passthrough itself rather than the differ's output.
 */
class DifferPropertyTest {
  @Test
  fun differPropertyReachesTheTestJvm() {
    assertEquals("pixelperfect", System.getProperty("uk.co.fractalmotion.mugshot.differ"))
  }
}

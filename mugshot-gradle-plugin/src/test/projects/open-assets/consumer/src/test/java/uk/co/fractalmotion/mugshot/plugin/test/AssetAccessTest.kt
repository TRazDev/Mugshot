package uk.co.fractalmotion.mugshot.plugin.test

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class AssetAccessTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun testViews() {
    val pairs = mapOf(
      "consumer/secret.txt" to "consumer",
      "producer1/secret.txt" to "producer1",
      "producer2/secret.txt" to "producer2",
      "external/secret.txt" to "external"
    )

    pairs.forEach { (key, value) ->
      val contents =
        mugshot.context.assets.open(key).bufferedReader().use { it.readText() }
      assertThat(contents).isEqualTo(value)
    }
  }
}

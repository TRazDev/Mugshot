package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.runtime.Composable
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import uk.co.fractalmotion.mugshot.Mugshot

class LoggerLeakTest {
  private val mugshot = Mugshot()
  private val expectExceptionRule = TestRule { base, _ ->
    object : Statement() {
      override fun evaluate() {
        var exception: Exception? = null
        try {
          base.evaluate()
        } catch (e: Exception) {
          exception = e
        }
        assert(exception != null)
      }
    }
  }

  @get:Rule val ignored: RuleChain = RuleChain.outerRule(expectExceptionRule).around(mugshot)

  @Test
  fun test1() {
    mugshot.snapshot { ComposeContent() }
  }

  @Test
  fun test2() {
    mugshot.snapshot { ComposeContent() }
  }

  @Composable
  private fun ComposeContent() {
    throw Exception()
  }
}

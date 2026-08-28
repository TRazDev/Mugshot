package uk.co.fractalmotion.mugshot.plugin.test

import uk.co.fractalmotion.mugshot.Mugshot
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(TestParameterInjector::class)
class DefaultLocaleTest(@TestParameter val locale: Locale) {
  enum class Locale(val tag: String?) {
    DEFAULT(null),
    FR("fr-rFR"),
    GB("en-rGB")
  }

  @get:Rule
  val chain: RuleChain = RuleChain
    .outerRule { base, _ ->
      object : Statement() {
        override fun evaluate() {
          try {
            locale.tag?.let { localeTag ->
              System.setProperty("uk.co.fractalmotion.mugshot.defaultLocale", localeTag)
            }
            base.evaluate()
          } catch (_: Exception) {
            System.clearProperty("uk.co.fractalmotion.mugshot.defaultLocale")
          }
        }
      }
    }
    .around(Mugshot())

  @Test
  fun `verify system property sets default locale`() {
    Mugshot().apply {
      snapshot(view = inflate(R.layout.title_color))
    }
  }
}

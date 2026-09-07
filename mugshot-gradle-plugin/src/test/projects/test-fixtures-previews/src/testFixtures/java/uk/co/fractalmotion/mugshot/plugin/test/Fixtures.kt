package uk.co.fractalmotion.mugshot.plugin.test

/**
 * Gives the module a Kotlin test fixtures compilation to generate into.
 *
 * The content is irrelevant -- what matters is that `compileDebugTestFixturesKotlin` exists and
 * has no Mugshot annotations on its compile classpath.
 */
public object Fixtures {
  public const val NAME: String = "fixture"
}

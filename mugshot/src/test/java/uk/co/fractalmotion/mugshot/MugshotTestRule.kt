package uk.co.fractalmotion.mugshot

import org.junit.rules.ExternalResource
import org.junit.rules.TemporaryFolder
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import org.junit.runners.model.Statement

class MugshotTestRule : ExternalResource() {
  private val tmpFolder = TemporaryFolder.builder().assureDeletion().build()
  private val reportDirKey = "mugshot.snapshot.dir"
  private var oldReportDir: String? = null

  internal lateinit var mugshot: Mugshot

  override fun before() {
    tmpFolder.create()

    oldReportDir = System.getProperty(reportDirKey)
    System.setProperty(reportDirKey, tmpFolder.newFolder().path)

    mugshot = Mugshot()
  }

  override fun after() {
    tmpFolder.delete()

    if (oldReportDir == null) {
      System.clearProperty(reportDirKey)
    } else {
      System.setProperty(reportDirKey, oldReportDir!!)
    }
  }

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        before()

        val errors: MutableList<Throwable> = ArrayList()
        try {
          mugshot.apply(base, description).evaluate()
        } catch (t: Throwable) {
          errors.add(t)
        } finally {
          try {
            after()
          } catch (t: Throwable) {
            errors.add(t)
          }
        }
        MultipleFailureException.assertEmpty(errors)
      }
    }
  }
}

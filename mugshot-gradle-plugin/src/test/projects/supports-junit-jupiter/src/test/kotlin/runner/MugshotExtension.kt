package runner

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.TestName

class MugshotExtension(
  val api: Mugshot
) : BeforeEachCallback, AfterEachCallback {

  override fun beforeEach(context: ExtensionContext) {
    api.setup(testName = context.toTestName())
  }

  override fun afterEach(context: ExtensionContext) {
    api.teardown()
  }

  private fun ExtensionContext.toTestName() =
    TestName(
      packageName = this.requiredTestClass.`package`?.name.orEmpty(),
      className = this.requiredTestClass.simpleName,
      methodName = this.requiredTestMethod.name
    )
}

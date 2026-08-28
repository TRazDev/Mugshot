package uk.co.fractalmotion.mugshot.internal

import uk.co.fractalmotion.mugshot.internal.MugshotLogger.MultipleFailuresException
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.io.FileNotFoundException

class MugshotLoggerTest {
  @Test
  fun testNoErrors() {
    val logger = MugshotLogger()

    try {
      logger.assertNoErrors()
    } catch (ignored: Exception) {
      fail("Did not expect exception to be thrown: $ignored")
    }
  }

  @Test
  fun testSingleError() {
    val logger = MugshotLogger()
    logger.error(FileNotFoundException("error1"), null)

    try {
      logger.assertNoErrors()
      fail("Expected exception to be thrown")
    } catch (ignored: Exception) {
      assertThat(ignored).isInstanceOf(FileNotFoundException::class.java)
    }
  }

  @Test
  fun testMultipleErrors() {
    val logger = MugshotLogger()
    logger.error(FileNotFoundException("error1"), null)
    logger.error("tag", null, IllegalStateException("error2"), null, null)

    try {
      logger.assertNoErrors()
      fail("Expected exceptions to be thrown")
    } catch (ignored: Exception) {
      assertThat(ignored).isInstanceOf(MultipleFailuresException::class.java)
      assertThat(ignored.message).contains("There were 2 errors:")
      assertThat(ignored.message).contains("java.io.FileNotFoundException: error1")
      assertThat(ignored.message).contains("java.lang.IllegalStateException: error2")
    }
  }

  @Test
  fun testFlushErrors() {
    val logger = MugshotLogger()
    logger.error(FileNotFoundException("error1"), null)
    logger.flushErrors()
    logger.assertNoErrors()
  }
}

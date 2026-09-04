@file:Suppress("DEPRECATION")

package uk.co.fractalmotion.mugshot

import sun.misc.Unsafe
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * Writes a value into a static final field.
 *
 * There is no supported way to do this. The JDK has been closing the routes off one at a time:
 * stripping the `final` modifier reflectively stopped working in JDK 12, and `Unsafe` was the
 * replacement. `AccessController` and `Unsafe`'s field offset methods are themselves now
 * deprecated for removal, so this stops compiling on some future JDK rather than degrading. CI
 * already runs JDK 24.
 *
 * Nothing to migrate to, as of September 2026:
 *
 *  * PowerMock, where this came from, has not shipped a commit since February 2022 and still
 *    imports `sun.misc.Unsafe`. Its own issue #1026 introduced this approach for "JDK12+", saying
 *    it "works on every JDK up to 13".
 *  * Upstream Paparazzi carries the same code, in both its library and its Gradle plugin.
 *  * `SessionParams.simulatedPlatformVersion` looks like the supported equivalent and is not.
 *    Measured with `compileSdk` 34 against layoutlib 16.2.1, whose own platform level is 36:
 *    setting it leaves application code reading `Build.VERSION.SDK_INT` as 36, while this
 *    reflection gives the expected 34. It configures layoutlib's internal behaviour, not what
 *    application code sees.
 *
 * Removing this would silently report the wrong SDK level to every consumer whose `compileSdk`
 * differs from layoutlib's, so it is suppressed rather than deleted. When the JDK finally removes
 * these, the honest options are a Java agent or dropping support for a `compileSdk` that does not
 * match layoutlib.
 *
 * Inspired by and ported from:
 * https://github.com/powermock/powermock/commit/fc092c5d7e339d01e079184a2a0e88b5c46fc0e8
 * https://github.com/powermock/powermock/commit/bd92bcc5329c4981cf09dece5c3eafcf92fe49ff
 */
internal fun Class<*>.getFieldReflectively(fieldName: String): Field =
  try {
    this.getDeclaredField(fieldName).also { it.isAccessible = true }
  } catch (e: NoSuchFieldException) {
    throw RuntimeException("Field '$fieldName' was not found in class $name.")
  }

internal fun Field.setStaticValue(value: Any) {
  try {
    this.isAccessible = true
    val isFinalModifierPresent = this.modifiers and Modifier.FINAL == Modifier.FINAL
    if (isFinalModifierPresent) {
      AccessController.doPrivileged<Any?>(
        PrivilegedAction {
          try {
            val unsafe = Unsafe::class.java.getFieldReflectively("theUnsafe").get(null) as Unsafe
            val offset = unsafe.staticFieldOffset(this)
            val base = unsafe.staticFieldBase(this)
            unsafe.setFieldValue(this, base, offset, value)
            null
          } catch (t: Throwable) {
            throw RuntimeException(t)
          }
        }
      )
    } else {
      this.set(null, value)
    }
  } catch (ex: SecurityException) {
    throw RuntimeException(ex)
  } catch (ex: IllegalAccessException) {
    throw RuntimeException(ex)
  } catch (ex: IllegalArgumentException) {
    throw RuntimeException(ex)
  }
}

internal fun Unsafe.setFieldValue(field: Field, base: Any, offset: Long, value: Any) =
  when (field.type) {
    Integer.TYPE -> this.putInt(base, offset, (value as Int))
    java.lang.Short.TYPE -> this.putShort(base, offset, (value as Short))
    java.lang.Long.TYPE -> this.putLong(base, offset, (value as Long))
    java.lang.Byte.TYPE -> this.putByte(base, offset, (value as Byte))
    java.lang.Boolean.TYPE -> this.putBoolean(base, offset, (value as Boolean))
    java.lang.Float.TYPE -> this.putFloat(base, offset, (value as Float))
    java.lang.Double.TYPE -> this.putDouble(base, offset, (value as Double))
    Character.TYPE -> this.putChar(base, offset, (value as Char))
    else -> this.putObject(base, offset, value)
  }

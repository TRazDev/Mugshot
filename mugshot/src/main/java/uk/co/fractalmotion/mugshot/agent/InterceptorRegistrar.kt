package uk.co.fractalmotion.mugshot.agent

import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.pool.TypePool
import java.util.logging.Logger

internal object InterceptorRegistrar {
  private val logger: Logger = Logger.getLogger(InterceptorRegistrar::class.java.name)
  private val byteBuddy = ByteBuddy()
  private val systemClassFileLocator = ClassFileLocator.ForClassLoader.ofSystemLoader()
  private val systemTypePool = TypePool.Default.ofSystemLoader()
  private val systemClassLoader = ClassLoader.getSystemClassLoader()

  private val methodInterceptors = mutableListOf<() -> Unit>()

  fun addMethodInterceptor(receiverClass: String, methodName: String, interceptor: Class<*>) =
    addMethodInterceptors(receiverClass, setOf(methodName to interceptor))

  fun addMethodInterceptors(receiverClass: String, methodNamesToInterceptors: Set<Pair<String, Class<*>>>) {
    val typeResolution = systemTypePool.describe(receiverClass)
    if (!typeResolution.isResolved) {
      // Silence here would show up as pixels rather than as an error: the render still happens,
      // with the method Mugshot meant to intercept behaving as it normally would.
      logger.warning(
        "Could not resolve $receiverClass, so ${methodNamesToInterceptors.joinToString { it.first }} " +
          "will not be intercepted and snapshots may not render as expected."
      )
      return
    }

    methodInterceptors += {
      var builder = byteBuddy
        .redefine<Any>(typeResolution.resolve(), systemClassFileLocator)

      methodNamesToInterceptors.forEach {
        builder = builder
          .method(ElementMatchers.named(it.first))
          .intercept(MethodDelegation.to(it.second))
      }

      builder
        .make()
        .load(systemClassLoader, ClassReloadingStrategy.fromInstalledAgent())
    }
  }

  fun registerMethodInterceptors() {
    methodInterceptors.forEach { it.invoke() }
  }

  /** Resets the registry between tests. Nothing in the library calls it. */
  fun clearMethodInterceptors() {
    methodInterceptors.clear()
  }
}

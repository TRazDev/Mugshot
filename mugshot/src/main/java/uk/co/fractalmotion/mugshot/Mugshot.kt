/*
 * Copyright (C) 2019 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.fractalmotion.mugshot

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.compose.runtime.Composable
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Date

public class Mugshot @JvmOverloads constructor(
  private val environment: Environment = detectEnvironment(),
  private val deviceConfig: DeviceConfig = DeviceConfig.NEXUS_5,
  private val theme: String = "android:Theme.Material.NoActionBar.Fullscreen",
  private val renderingMode: RenderingMode = RenderingMode.NORMAL,
  private val appCompatEnabled: Boolean = true,
  private val maxPercentDifference: Double = detectMaxPercentDifferenceDefault(),
  private val snapshotHandler: SnapshotHandler = determineHandler(maxPercentDifference),
  private val renderExtensions: Set<RenderExtension> = setOf(),
  private val supportsRtl: Boolean = false,
  private val showSystemUi: Boolean = false,
  private val useDeviceResolution: Boolean = false
) : TestRule {
  private lateinit var sdk: MugshotSdk
  private lateinit var frameHandler: SnapshotHandler.FrameHandler
  private var testName: TestName? = null

  public val layoutInflater: LayoutInflater
    get() = sdk.layoutInflater

  public val resources: Resources
    get() = sdk.resources

  public val context: Context
    get() = sdk.context

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        setup(testName = description.toTestName())
        try {
          base.evaluate()
        } finally {
          teardown()
        }
      }
    }
  }

  public fun prepare(description: Description) {
    testName = description.toTestName()
    sdk.prepare()
  }

  public fun setup(testName: TestName) {
    sdk = MugshotSdk(
      environment = environment,
      deviceConfig = deviceConfig,
      theme = theme,
      renderingMode = renderingMode,
      appCompatEnabled = appCompatEnabled,
      renderExtensions = renderExtensions,
      supportsRtl = supportsRtl,
      showSystemUi = showSystemUi,
      onNewFrame = { frameHandler.handle(it) },
      useDeviceResolution = useDeviceResolution
    )
    sdk.setup()
    this.testName = testName
    sdk.prepare()
  }

  public fun teardown() {
    testName = null
    sdk.teardown()
    snapshotHandler.close()
  }

  @Deprecated("Please use teardown() instead.")
  public fun close() {
    testName = null
    sdk.teardown()
    snapshotHandler.close()
  }

  public fun <V : View> inflate(@LayoutRes layoutId: Int): V = sdk.inflate(layoutId)

  public fun snapshot(name: String? = null, composable: @Composable () -> Unit) {
    createFrameHandler(name).use { handler ->
      frameHandler = handler
      sdk.snapshot(composable)
    }
  }

  @JvmOverloads
  public fun snapshot(view: View, name: String? = null, offsetMillis: Long = 0L) {
    createFrameHandler(name).use { handler ->
      frameHandler = handler
      sdk.snapshot(view, offsetMillis)
    }
  }

  public fun unsafeUpdateConfig(
    deviceConfig: DeviceConfig? = null,
    theme: String? = null,
    renderingMode: RenderingMode? = null
  ): Unit = sdk.unsafeUpdateConfig(deviceConfig, theme, renderingMode)

  private fun createFrameHandler(name: String? = null): SnapshotHandler.FrameHandler {
    val snapshot = Snapshot(name, testName!!, Date())
    return snapshotHandler.newFrameHandler(snapshot)
  }

  private fun Description.toTestName(): TestName {
    val fullQualifiedName = className
    val packageName = fullQualifiedName.substringBeforeLast('.', missingDelimiterValue = "")
    val className = fullQualifiedName.substringAfterLast('.')
    return TestName(packageName, className, methodName)
  }

  private companion object {
    private val isVerifying: Boolean =
      System.getProperty("mugshot.test.verify")?.toBoolean() == true

    private fun determineHandler(maxPercentDifference: Double): SnapshotHandler =
      if (isVerifying) {
        SnapshotVerifier(maxPercentDifference)
      } else {
        HtmlReportWriter(maxPercentDifference = maxPercentDifference)
      }
  }
}

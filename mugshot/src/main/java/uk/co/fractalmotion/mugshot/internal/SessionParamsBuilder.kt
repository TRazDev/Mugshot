/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.fractalmotion.mugshot.internal

import com.android.SdkConstants
import com.android.ide.common.rendering.api.AssetRepository
import com.android.ide.common.rendering.api.ResourceNamespace
import com.android.ide.common.rendering.api.ResourceReference
import com.android.ide.common.rendering.api.SessionParams
import com.android.ide.common.rendering.api.SessionParams.Key
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.ide.common.resources.ResourceRepository
import com.android.ide.common.resources.ResourceResolver
import com.android.ide.common.resources.ResourceValueMap
import com.android.ide.common.resources.configuration.FolderConfiguration
import com.android.ide.common.resources.getConfiguredResources
import com.android.layoutlib.bridge.Bridge
import com.android.resources.LayoutDirection
import com.android.resources.ResourceType
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.internal.parsers.LayoutPullParser
import uk.co.fractalmotion.mugshot.internal.resources.pseudolocalizeIfNeeded

/** Creates [SessionParams] objects. */
internal data class SessionParamsBuilder(
  private val layoutlibCallback: MugshotCallback,
  private val logger: MugshotLogger,
  private val frameworkResources: ResourceRepository,
  private val assetRepository: AssetRepository,
  private val projectResources: ResourceRepository,
  private val deviceConfig: DeviceConfig = DeviceConfig.NEXUS_5,
  private val renderingMode: RenderingMode = RenderingMode.NORMAL,
  private val targetSdk: Int = 22,
  private val flags: Map<Key<*>, Any> = mapOf(),
  private val themeName: String? = null,
  private val isProjectTheme: Boolean = false,
  private val layoutPullParser: LayoutPullParser? = null,
  private val projectKey: Any? = null,
  private val minSdk: Int = 0,
  private val decor: Boolean = true,
  private val supportsRtl: Boolean = false
) {
  fun withTheme(themeName: String, isProjectTheme: Boolean): SessionParamsBuilder {
    return copy(themeName = themeName, isProjectTheme = isProjectTheme)
  }

  fun withTheme(themeName: String): SessionParamsBuilder {
    return when {
      themeName.startsWith(SdkConstants.PREFIX_ANDROID) -> {
        withTheme(themeName.substring(SdkConstants.PREFIX_ANDROID.length), false)
      }
      else -> withTheme(themeName, true)
    }
  }

  fun plusFlag(flag: SessionParams.Key<*>, value: Any) = copy(flags = flags + (flag to value))

  /**
   * The framework's resources for one folder configuration, resolved once per configuration.
   *
   * Resolving them is the single most expensive thing a session does -- measured at 2.4ms of a
   * 22ms screenshot -- and the answer depends only on the configuration. A matrix of previews
   * asks for the same handful of configurations over and over: 24 of them across hundreds of
   * cases, so all but the first two dozen resolutions were repeats.
   *
   * Safe to share because the platform's resources do not change within a JVM: the repository is
   * built once, and what comes back is read, never written. Keyed by the repository as well as
   * the configuration so a second repository cannot read the first one's answers.
   */
  private fun configuredFrameworkResources(
    folderConfiguration: FolderConfiguration
  ): Map<ResourceType, ResourceValueMap> {
    val key = FrameworkResourceKey(frameworkResources, folderConfiguration.qualifierString)
    return frameworkResourceCache.getOrPut(key) {
      frameworkResources.getConfiguredResources(folderConfiguration)
        .pseudolocalizeIfNeeded(folderConfiguration.localeQualifier)
        .row(ResourceNamespace.ANDROID)
    }
  }

  fun build(): SessionParams {
    require(themeName != null)

    val folderConfiguration = deviceConfig.folderConfiguration
    val resourceResolver = ResourceResolver.create(
      mapOf<ResourceNamespace, Map<ResourceType, ResourceValueMap>>(
        ResourceNamespace.ANDROID to configuredFrameworkResources(folderConfiguration),
        *projectResources.getConfiguredResources(folderConfiguration)
          .pseudolocalizeIfNeeded(folderConfiguration.localeQualifier)
          .rowMap()
          .map { (key, value) -> key to value }
          .toTypedArray()
      ),
      ResourceReference(
        if (isProjectTheme) ResourceNamespace.RES_AUTO else ResourceNamespace.ANDROID,
        ResourceType.STYLE,
        themeName
      )
    )

    val result = SessionParams(
      layoutPullParser, renderingMode, projectKey,
      deviceConfig.hardwareConfig, resourceResolver, layoutlibCallback, minSdk, targetSdk, logger
    )
    result.fontScale = deviceConfig.fontScale
    result.uiMode = deviceConfig.uiModeMask

    val localeQualifier = folderConfiguration.localeQualifier
    val layoutDirectionQualifier = folderConfiguration.layoutDirectionQualifier
    // https://cs.android.com/android-studio/platform/tools/adt/idea/+/mirror-goog-studio-main:rendering/src/com/android/tools/rendering/RenderTask.java;l=705;drc=1c797047cbbecd4c6dba47ea5c3c93703d4d79d0
    if (LayoutDirection.RTL == layoutDirectionQualifier.value && !Bridge.isLocaleRtl(localeQualifier.tag)) {
      result.locale = "ur"
    } else {
      result.locale = localeQualifier.tag
    }
    result.setRtlSupport(supportsRtl)

    // The map holds Key<*> to Any, so each key's type argument is already lost. Pairing was
    // enforced by plusFlag when the entry went in.
    @Suppress("UNCHECKED_CAST")
    for ((key, value) in flags) {
      result.setFlag(key as Key<Any>, value)
    }
    result.setAssetRepository(assetRepository)

    if (!decor) {
      result.setForceNoDecor()
    }

    return result
  }
}

/** Identity of the repository plus the configuration whose resources were resolved from it. */
private data class FrameworkResourceKey(
  private val repository: ResourceRepository,
  private val qualifiers: String
)

private val frameworkResourceCache =
  java.util.concurrent.ConcurrentHashMap<FrameworkResourceKey, Map<ResourceType, ResourceValueMap>>()

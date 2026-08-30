package uk.co.fractalmotion.mugshot.preview.lints

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.google.auto.service.AutoService

@AutoService(value = [IssueRegistry::class])
public class MugshotPreviewIssueRegistry : IssueRegistry() {
  override val issues: List<Issue> = listOf(
    MugshotPreviewDetector.COMPOSABLE_NOT_DETECTED,
    MugshotPreviewDetector.PREVIEW_NOT_DETECTED,
    MugshotPreviewDetector.PRIVATE_PREVIEW_DETECTED,
    MugshotPreviewDetector.PREVIEW_ARGUMENTS_IGNORED
  )

  override val api: Int = CURRENT_API

  override val vendor: Vendor = Vendor(
    vendorName = "TRazDev/Mugshot",
    identifier = "uk.co.fractalmotion.mugshot",
    feedbackUrl = "https://github.com/TRazDev/Mugshot/issues"
  )
}

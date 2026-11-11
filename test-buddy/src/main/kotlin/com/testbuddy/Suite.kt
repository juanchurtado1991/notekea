package com.testbuddy

import androidx.compose.runtime.Composable
import java.nio.file.Path

/** A host app's cases and what Test Buddy needs to run them; made with [testBuddy]. */
public class Suite internal constructor(
    public val appName: String,
    public val version: String,
    public val language: TesterLanguage,
    public val automated: List<AutomatedCase>,
    public val manual: List<ManualCase>,
    internal val window: WindowSize,
    internal val dataSources: List<Path>,
    internal val diagnostics: () -> String?,
    internal val content: @Composable (CaseFolders) -> Unit
) {
    /** The same suite with other cases: a part of it, or the cases of one feature. */
    internal fun withCases(
        automated: List<AutomatedCase>,
        manual: List<ManualCase>
    ): Suite = Suite(
        appName = appName,
        version = version,
        language = language,
        automated = automated,
        manual = manual,
        window = window,
        dataSources = dataSources,
        diagnostics = diagnostics,
        content = content
    )
}

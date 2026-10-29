package com.testbuddy

import androidx.compose.runtime.Composable
import java.nio.file.Path

/** A host app's cases and what Test Buddy needs to run them; made with [testBuddy]. */
public class Suite internal constructor(
    public val appName: String,
    public val version: String,
    public val automated: List<AutomatedCase>,
    public val manual: List<ManualCase>,
    internal val window: WindowSize,
    internal val dataSources: List<Path>,
    internal val backUp: () -> List<Path>,
    internal val installedApp: (HostSystem) -> Path?,
    internal val content: @Composable (CaseFolders) -> Unit
) {
    /** The same suite with other cases: a part of it, or the cases of one feature. */
    internal fun withCases(
        automated: List<AutomatedCase>,
        manual: List<ManualCase>
    ): Suite = Suite(
        appName = appName,
        version = version,
        automated = automated,
        manual = manual,
        window = window,
        dataSources = dataSources,
        backUp = backUp,
        installedApp = installedApp,
        content = content
    )
}

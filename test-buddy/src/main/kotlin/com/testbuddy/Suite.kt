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
    internal val content: @Composable (CaseFolders) -> Unit
)

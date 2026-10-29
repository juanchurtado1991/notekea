package com.testbuddy

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isDisplayed

/**
 * Whether the node is on screen now; a node that went away between finding it and looking at it (a row removed, a
 * screen changing) is not, instead of failing the case as `isDisplayed` does.
 */
public fun SemanticsNodeInteraction.isShown(): Boolean = runCatching {
    isDisplayed()
}.getOrDefault(defaultValue = false)

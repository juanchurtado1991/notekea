package com.testbuddy.app

import androidx.compose.runtime.staticCompositionLocalOf

/** The manager's words in the suite's language, provided by its window. */
internal val LocalTesterTexts = staticCompositionLocalOf { TesterTexts.English }

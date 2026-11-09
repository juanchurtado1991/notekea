package com.testbuddy.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Test Buddy's own quiet look, the same for every host app. */
@Composable
internal fun AutoTestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AutoTestColors.Running,
            background = AutoTestColors.Page,
            surface = Color.White,
            surfaceVariant = AutoTestColors.Quiet
        ),
        content = content
    )
}

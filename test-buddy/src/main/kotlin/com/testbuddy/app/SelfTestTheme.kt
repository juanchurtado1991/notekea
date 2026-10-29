package com.testbuddy.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Test Buddy's own quiet look, the same for every host app. */
@Composable
internal fun SelfTestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SelfTestColors.Running,
            background = SelfTestColors.Page,
            surface = Color.White,
            surfaceVariant = SelfTestColors.Quiet
        ),
        content = content
    )
}

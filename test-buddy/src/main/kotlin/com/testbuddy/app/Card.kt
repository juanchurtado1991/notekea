package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A white card with a thin border, the Self-Test's one container. */
@Composable
internal fun Card(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = shape)
            .border(width = 1.dp, color = SelfTestColors.Border, shape = shape)
            .padding(all = 20.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp),
        content = content
    )
}

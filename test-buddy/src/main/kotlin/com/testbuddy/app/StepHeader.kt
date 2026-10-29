package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The window's top: the Self-Test's name, the app and the machine, and the three steps with the current one lit. */
@Composable
internal fun StepHeader(
    title: String,
    subtitle: String,
    step: SelfTestStep
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        Column {
            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 13.sp, color = SelfTestColors.Muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            listOf("1  Automatic", "2  Manual", "3  Send report").forEachIndexed { index, label ->
                StepChip(label = label, isDone = index < step.ordinal, isCurrent = index == step.ordinal)
            }
        }
    }
}

@Composable
private fun StepChip(
    label: String,
    isDone: Boolean,
    isCurrent: Boolean
) {
    val color = when {
        isCurrent -> SelfTestColors.Running
        isDone -> SelfTestColors.Passed
        else -> SelfTestColors.Waiting
    }
    Text(
        text = label,
        color = if (isCurrent) Color.White else color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(color = if (isCurrent) color else color.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

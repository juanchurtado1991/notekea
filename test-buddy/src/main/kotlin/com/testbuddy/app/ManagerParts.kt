package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A small rounded count in a color: `✓ 31 passed`. */
@Composable
internal fun CountChip(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = color,
        modifier = Modifier
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(size = 10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** A section's small capital title: NOW, RECENT. */
@Composable
internal fun SectionLabel(text: String) {
    Text(text = text, fontSize = 10.sp, color = SelfTestColors.Muted, fontWeight = FontWeight.SemiBold)
}

/**
 * Steps as a line: the [done] ones with ✓, the [current] one in bold with …, the [pending] ones grey; how both the
 * running automatic case and the manual case show where they are.
 */
@Composable
internal fun StepTimeline(
    done: List<String>,
    current: String?,
    pending: List<String> = emptyList()
) {
    done.forEach { StepRow(text = it, status = CaseStatus.PASSED) }
    current?.let { StepRow(text = it, status = CaseStatus.RUNNING) }
    pending.forEach { StepRow(text = it, status = CaseStatus.WAITING) }
}

@Composable
private fun StepRow(
    text: String,
    status: CaseStatus
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), verticalAlignment = Alignment.Top) {
        StatusBadge(status = status)
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (status == CaseStatus.RUNNING) FontWeight.SemiBold else FontWeight.Normal,
            color = if (status == CaseStatus.WAITING) SelfTestColors.Muted else Color.Unspecified
        )
    }
}

package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A case's state as a small round badge: ✓ passed, ✕ failed, … running, · waiting or skipped. */
@Composable
internal fun StatusBadge(status: CaseStatus) {
    val color = status.color()
    Box(
        modifier = Modifier.size(size = 20.dp).background(color = color.copy(alpha = 0.15f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = status.symbol(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun CaseStatus.color() = when (this) {
    CaseStatus.PASSED -> SelfTestColors.Passed
    CaseStatus.FAILED -> SelfTestColors.Failed
    CaseStatus.RUNNING -> SelfTestColors.Running
    CaseStatus.WAITING, CaseStatus.SKIPPED -> SelfTestColors.Waiting
}

private fun CaseStatus.symbol() = when (this) {
    CaseStatus.PASSED -> "✓"
    CaseStatus.FAILED -> "✕"
    CaseStatus.RUNNING -> "…"
    CaseStatus.WAITING, CaseStatus.SKIPPED -> "·"
}

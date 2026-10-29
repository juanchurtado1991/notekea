package com.testbuddy.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testbuddy.AutomatedCase
import com.testbuddy.CaseResult

/** Step 1: what the automatic part does, then its progress case by case, then what failed and the way on. */
@Composable
internal fun AutomaticStep(
    appName: String,
    cases: List<AutomatedCase>,
    state: SelfTestState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit
) {
    val done = state.results.size
    val failed = state.results.values.count { it is CaseResult.Failed }
    val hasRun = done > 0 && !state.isRunning
    Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        val hint = automaticHint(appName = appName, isRunning = state.isRunning, hasRun = hasRun)
        Text(text = hint, fontSize = 13.sp, color = SelfTestColors.Muted)
        if (done > 0 || state.isRunning) {
            AutomaticProgress(done = done, total = cases.size, failed = failed)
            CaseList(cases = cases, state = state)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                state.isRunning -> OutlinedButton(onClick = onStop) { Text(text = "Stop") }
                hasRun -> OutlinedButton(onClick = onStart) { Text(text = "Run again") }
                else -> Button(onClick = onStart) { Text(text = "Start the automatic tests") }
            }
            Spacer(modifier = Modifier.weight(weight = 1f))
            Button(onClick = onNext, enabled = !state.isRunning) { Text(text = "Next: manual cases") }
        }
    }
}

private fun automaticHint(
    appName: String,
    isRunning: Boolean,
    hasRun: Boolean
): String = when {
    isRunning -> "You can keep using the computer while this runs."

    hasRun ->
        "Done. The failures are in the report: there is nothing to do about them now. Close $appName " +
            "before you go on: its settings are saved aside first, and put back when you finish."

    else -> "The program checks most cases on its own, on copies of the test data. It takes a few minutes."
}

@Composable
private fun AutomaticProgress(
    done: Int,
    total: Int,
    failed: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Row {
            Text(text = "$done of $total automatic cases", fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(weight = 1f))
            if (failed > 0) Text(text = "$failed failed", fontSize = 13.sp, color = SelfTestColors.Failed)
        }
        LinearProgressIndicator(
            progress = { if (total == 0) 1f else done / total.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CaseList(
    cases: List<AutomatedCase>,
    state: SelfTestState
) {
    val listState = rememberLazyListState()
    val runningIndex = cases.indexOfFirst { it.id == state.runningId }
    LaunchedEffect(runningIndex) { if (runningIndex >= 0) listState.animateScrollToItem(index = runningIndex) }
    Card {
        LazyColumn(state = listState, modifier = Modifier.heightIn(max = 300.dp)) {
            items(items = cases, key = { it.id }) { case ->
                CaseRow(
                    case = case,
                    status = CaseStatus.of(id = case.id, state = state),
                    result = state.results[case.id]
                )
            }
        }
    }
}

@Composable
private fun CaseRow(
    case: AutomatedCase,
    status: CaseStatus,
    result: CaseResult?
) {
    val background = if (status == CaseStatus.RUNNING) SelfTestColors.Running.copy(alpha = 0.10f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = background, shape = RoundedCornerShape(size = 6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBadge(status = status)
        Spacer(modifier = Modifier.width(width = 10.dp))
        Text(text = case.id, fontSize = 12.sp, color = SelfTestColors.Muted, modifier = Modifier.width(width = 64.dp))
        Column {
            Text(text = case.title, fontSize = 13.sp)
            if (result is CaseResult.Failed) {
                Text(
                    text = result.message,
                    fontSize = 12.sp,
                    color = SelfTestColors.Failed
                )
            }
        }
    }
}

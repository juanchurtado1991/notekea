package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testbuddy.AutomatedCase
import com.testbuddy.CaseResult
import kotlinx.coroutines.delay

/**
 * The automatic cases: before a run, what they do and Start; while one runs, "Running tests" with the time, the
 * progress and what is left, the counts, the case it runs NOW with its steps, the RECENT ones and Stop; after it, the
 * counts and the failures, with Run again and the way on to the manual cases.
 */
@Composable
internal fun ColumnScope.AutomaticTab(
    cases: List<AutomatedCase>,
    state: ManagerState,
    actions: ManagerActions
) {
    when {
        state.isRunning -> Running(cases = cases, state = state, onStop = actions.onStop)
        state.results.isEmpty() -> Idle(total = cases.size, onStart = actions.onStart)
        else -> Finished(cases = cases, state = state, actions = actions)
    }
}

@Composable
private fun ColumnScope.Idle(
    total: Int,
    onStart: () -> Unit
) {
    Text(text = "Automatic tests", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Text(
        text = "$total cases check the app on their own, on copies of the test data, out of sight. It takes a while " +
            "and you can keep using the computer.",
        fontSize = 12.sp,
        color = SelfTestColors.Muted
    )
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text(text = "Start the automatic tests") }
    Spacer(modifier = Modifier.weight(weight = 1f))
}

@Composable
private fun ColumnScope.Running(
    cases: List<AutomatedCase>,
    state: ManagerState,
    onStop: () -> Unit
) {
    val elapsed = rememberElapsedMillis(startedAt = state.startedAt)
    val done = state.results.size
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(size = 16.dp), strokeWidth = 2.dp)
        Text(
            text = "Running tests",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp).weight(weight = 1f)
        )
        Text(text = clock(millis = elapsed), fontSize = 12.sp, color = SelfTestColors.Muted)
    }
    LinearProgressIndicator(progress = {
        done / cases.size.coerceAtLeast(minimumValue = 1).toFloat()
    }, modifier = Modifier.fillMaxWidth())
    Text(
        text = "$done of ${cases.size}" + timeLeft(done, cases.size, elapsed),
        fontSize = 11.sp,
        color = SelfTestColors.Muted
    )
    Counts(results = state.results.values, waiting = cases.size - done)
    NowCard(case = cases.firstOrNull { it.id == state.runningId }, steps = state.runningSteps)
    Recent(cases = cases, results = state.results.values.toList())
    Spacer(modifier = Modifier.weight(weight = 1f))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "You can keep using the computer.",
            fontSize = 11.sp,
            color = SelfTestColors.Muted,
            modifier = Modifier.weight(weight = 1f)
        )
        OutlinedButton(onClick = onStop) { Text(text = "Stop") }
    }
}

@Composable
private fun NowCard(
    case: AutomatedCase?,
    steps: List<String>
) {
    Card {
        SectionLabel(text = "NOW")
        Text(
            text = case?.let {
                "${it.id} · ${it.title}"
            } ?: "Starting…",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        StepTimeline(done = steps.dropLast(n = 1), current = steps.lastOrNull())
    }
}

@Composable
private fun Recent(
    cases: List<AutomatedCase>,
    results: List<CaseResult>
) {
    if (results.isEmpty()) return
    SectionLabel(text = "RECENT")
    results.takeLast(n = RECENT).asReversed().forEach { result ->
        ResultRow(title = cases.firstOrNull { it.id == result.id }?.title.orEmpty(), result = result)
    }
}

@Composable
private fun ColumnScope.Finished(
    cases: List<AutomatedCase>,
    state: ManagerState,
    actions: ManagerActions
) {
    val failed = state.results.values.filterIsInstance<CaseResult.Failed>()
    Text(
        text = if (failed.isEmpty()) "All automatic tests passed" else "Automatic tests done",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    Counts(results = state.results.values, waiting = cases.size - state.results.size)
    if (failed.isNotEmpty()) {
        Text(
            text = "The failures go in the report: there is nothing to do about them now.",
            fontSize = 12.sp,
            color = SelfTestColors.Muted
        )
        failed.take(n = SHOWN_FAILURES).forEach { result ->
            ResultRow(title = cases.firstOrNull { it.id == result.id }?.title.orEmpty(), result = result)
        }
    }
    Spacer(modifier = Modifier.weight(weight = 1f))
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        OutlinedButton(onClick = actions.onStart) { Text(text = "Run again") }
        Spacer(modifier = Modifier.weight(weight = 1f))
        Button(onClick = { actions.onTab(ManagerTab.MANUAL) }) { Text(text = "Next: manual cases ›") }
    }
}

@Composable
private fun Counts(
    results: Collection<CaseResult>,
    waiting: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CountChip(text = "✓ ${results.count { it is CaseResult.Passed }} passed", color = SelfTestColors.Passed)
        CountChip(text = "✕ ${results.count { it is CaseResult.Failed }} failed", color = SelfTestColors.Failed)
        if (waiting > 0) CountChip(text = "$waiting to go", color = SelfTestColors.Muted)
    }
}

@Composable
private fun ResultRow(
    title: String,
    result: CaseResult
) {
    val status = when (result) {
        is CaseResult.Passed -> CaseStatus.PASSED
        is CaseResult.Failed -> CaseStatus.FAILED
        is CaseResult.Skipped -> CaseStatus.SKIPPED
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        StatusBadge(status = status)
        Text(text = "${result.id} · $title", fontSize = 12.sp, maxLines = 1)
    }
}

/** The milliseconds since [startedAt], counting each second. */
@Composable
private fun rememberElapsedMillis(startedAt: Long?): Long {
    var now by remember { mutableLongStateOf(value = System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(timeMillis = SECOND_MILLIS)
        }
    }
    return startedAt?.let { (now - it).coerceAtLeast(minimumValue = 0) } ?: 0
}

private fun clock(millis: Long): String {
    val seconds = millis / SECOND_MILLIS
    return "%02d:%02d".format(seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
}

/** ` · about 14 minutes left`, from the time the cases so far took; nothing until a case is done. */
private fun timeLeft(
    done: Int,
    total: Int,
    elapsed: Long
): String {
    if (done == 0 || done >= total) return ""
    val minutes = (elapsed / done * (total - done) / SECOND_MILLIS / SECONDS_PER_MINUTE).coerceAtLeast(minimumValue = 1)
    return " · about $minutes minute${if (minutes == 1L) "" else "s"} left"
}

private const val RECENT = 3
private const val SHOWN_FAILURES = 6
private const val SECOND_MILLIS = 1_000L
private const val SECONDS_PER_MINUTE = 60

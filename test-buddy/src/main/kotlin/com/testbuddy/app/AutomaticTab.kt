package com.testbuddy.app

import androidx.compose.foundation.layout.Arrangement
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
internal fun AutomaticTab(
    cases: List<AutomatedCase>,
    state: ManagerState,
    actions: ManagerActions
) {
    when {
        state.isRunning -> Running(
            cases = cases,
            state = state,
            onStop = actions.onStop
        )

        state.results.isEmpty() -> Idle(
            total = cases.size,
            onStart = actions.onStart
        )

        else -> Finished(
            cases = cases,
            state = state,
            actions = actions
        )
    }
}

@Composable
private fun Idle(
    total: Int,
    onStart: () -> Unit
) = TabLayout(actions = {}) {
    Text(
        text = LocalTesterTexts.current.automaticTests,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Text(
        text = LocalTesterTexts.current.automaticIntro(total),
        fontSize = 12.sp,
        color = AutoTestColors.Muted
    )

    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth()
    ) { Text(text = LocalTesterTexts.current.startAutomatic) }
}

@Composable
private fun Running(
    cases: List<AutomatedCase>,
    state: ManagerState,
    onStop: () -> Unit
) = TabLayout(actions = {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = LocalTesterTexts.current.keepUsing,
            fontSize = 11.sp,
            color = AutoTestColors.Muted,
            modifier = Modifier.weight(weight = 1f)
        )

        OutlinedButton(onClick = onStop) { Text(text = LocalTesterTexts.current.stop) }
    }
}) {
    val elapsed = rememberElapsedMillis(startedAt = state.startedAt)
    val done = state.results.size

    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(size = 16.dp),
            strokeWidth = 2.dp
        )

        Text(
            text = LocalTesterTexts.current.runningTests,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp).weight(weight = 1f)
        )

        Text(
            text = clock(millis = elapsed),
            fontSize = 12.sp,
            color = AutoTestColors.Muted
        )
    }

    LinearProgressIndicator(
        progress = {
            done / cases
                .size
                .coerceAtLeast(minimumValue = 1)
                .toFloat()
        },
        modifier = Modifier.fillMaxWidth()
    )

    val texts = LocalTesterTexts.current

    Text(
        text = texts.doneOf(done, cases.size) + timeLeft(done, cases.size, elapsed, texts),
        fontSize = 11.sp,
        color = AutoTestColors.Muted
    )

    Counts(
        results = state.results.values,
        waiting = cases.size - done
    )

    NowCard(
        steps = state.runningSteps,
        case = cases.firstOrNull {
            it.id == state.runningId
        }
    )

    Recent(
        cases = cases,
        results = state
            .results
            .values
            .toList()
    )
}

@Composable
private fun NowCard(
    steps: List<String>,
    case: AutomatedCase?
) {
    Card {
        SectionLabel(text = LocalTesterTexts.current.now)

        Text(
            text = case?.let { "${it.id} · ${it.title}" } ?: LocalTesterTexts.current.starting,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        StepTimeline(
            done = steps.dropLast(n = 1),
            current = steps.lastOrNull()
        )
    }
}

@Composable
private fun Recent(
    cases: List<AutomatedCase>,
    results: List<CaseResult>
) {
    if (results.isEmpty()) return
    SectionLabel(text = LocalTesterTexts.current.recent)

    results
        .takeLast(n = RECENT)
        .asReversed()
        .forEach { result ->
            ResultRow(
                title = cases
                    .firstOrNull {
                        it.id == result.id
                    }
                    ?.title
                    .orEmpty(),
                result = result
            )
        }
}

@Composable
private fun Finished(
    cases: List<AutomatedCase>,
    state: ManagerState,
    actions: ManagerActions
) = TabLayout(actions = {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        OutlinedButton(onClick = actions.onStart) { Text(text = LocalTesterTexts.current.runAgain) }

        Spacer(modifier = Modifier.weight(weight = 1f))

        Button(onClick = {
            actions.onTab(ManagerTab.MANUAL)
        }) { Text(text = LocalTesterTexts.current.nextManual) }
    }
}) {
    val failed = state
        .results
        .values
        .filterIsInstance<CaseResult.Failed>()

    val texts = LocalTesterTexts.current
    val title = if (failed.isEmpty()) texts.allPassed else texts.automaticDone

    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Counts(
        results = state.results.values,
        waiting = cases.size - state.results.size
    )

    UnmetNote(
        cases = cases,
        results = state.results.values
    )

    if (failed.isNotEmpty()) {
        Text(
            text = LocalTesterTexts.current.failuresInReport,
            fontSize = 12.sp,
            color = AutoTestColors.Muted
        )

        failed.take(n = SHOWN_FAILURES).forEach { result ->
            ResultRow(
                title = cases
                    .firstOrNull {
                        it.id == result.id
                    }
                    ?.title
                    .orEmpty(),
                result = result
            )
        }
    }
}

@Composable
private fun Counts(
    results: Collection<CaseResult>,
    waiting: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CountChip(
            text = LocalTesterTexts.current.passedCount(
                results.count {
                    it is CaseResult.Passed
                }
            ),
            color = AutoTestColors.Passed
        )

        CountChip(
            text = LocalTesterTexts.current.failedCount(
                results.count {
                    it is CaseResult.Failed
                }
            ),
            color = AutoTestColors.Failed
        )

        if (waiting > 0) {
            CountChip(
                text = LocalTesterTexts.current.toGo(waiting),
                color = AutoTestColors.Muted
            )
        }
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBadge(status = status)

        Text(
            text = "${result.id} · $title",
            fontSize = 12.sp,
            maxLines = 1
        )
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
    elapsed: Long,
    texts: TesterTexts
): String {
    if (done == 0 || done >= total) return ""

    val minutes =
        (elapsed / done * (total - done) / SECOND_MILLIS / SECONDS_PER_MINUTE).coerceAtLeast(
            minimumValue = 1
        )

    return texts.minutesLeft(minutes)
}

private const val RECENT = 3
private const val SHOWN_FAILURES = 6
private const val SECOND_MILLIS = 1_000L
private const val SECONDS_PER_MINUTE = 60

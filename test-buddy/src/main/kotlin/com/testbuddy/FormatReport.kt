package com.testbuddy

import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

/**
 * The plain-text report a tester sends back, so it pastes into any chat: the app and the machine, then the automatic
 * and the manual results, each failure on a line of its own with its ID, title and message, and [diagnostics] last.
 */
public fun formatReport(
    suite: Suite,
    environment: ReportEnvironment,
    automatic: List<CaseResult>,
    manual: List<ManualAnswer>,
    diagnostics: String? = null
): String = buildString {
    appendLine("${suite.appName} Self-Test report")
    appendLine(
        "${suite.appName} ${suite.version} · ${environment.system} · ${environment.language} · ${environment.screen}"
    )
    appendAutomatic(suite = suite, results = automatic)
    appendManual(suite = suite, answers = manual)
    if (!diagnostics.isNullOrBlank()) {
        appendLine("Diagnostics:")
        appendLine(diagnostics.trimEnd())
    }
}

private fun StringBuilder.appendAutomatic(
    suite: Suite,
    results: List<CaseResult>
) {
    val passed = results.count { it is CaseResult.Passed }
    val failed = results.filterIsInstance<CaseResult.Failed>()
    val skipped = results.count { it is CaseResult.Skipped }
    appendLine("Automatic: $passed passed, ${failed.size} failed, $skipped skipped (${totalTime(results = results)})")
    failed.forEach { result ->
        val title = suite.automated.firstOrNull { it.id == result.id }?.title.orEmpty()
        val screenshot = result.screenshot?.let { " (screenshot: ${it.fileName})" }.orEmpty()
        appendLine("  ❌ ${result.id} $title: ${result.message}$screenshot")
    }
}

private fun StringBuilder.appendManual(
    suite: Suite,
    answers: List<ManualAnswer>
) {
    val passed = answers.count { it is ManualAnswer.Passed }
    val failed = answers.filterIsInstance<ManualAnswer.Failed>()
    val notRun = answers.count { it is ManualAnswer.NotRun }
    appendLine("Manual: $passed passed, ${failed.size} failed, $notRun not run")
    failed.forEach { answer ->
        val title = suite.manual.firstOrNull { it.id == answer.id }?.title.orEmpty()
        val screenshot = answer.screenshot?.let { " (screenshot: $it)" }.orEmpty()
        appendLine("  ❌ ${answer.id} $title: \"${answer.note}\"$screenshot")
    }
}

private fun totalTime(results: List<CaseResult>): String {
    val total = results.fold(ZERO) { sum, result -> sum + duration(result = result) }
    return if (total.inWholeMinutes > 0) "${total.inWholeMinutes} min" else "${total.inWholeSeconds} s"
}

private fun duration(result: CaseResult): Duration = when (result) {
    is CaseResult.Passed -> result.duration
    is CaseResult.Failed -> result.duration
    is CaseResult.Skipped -> ZERO
}

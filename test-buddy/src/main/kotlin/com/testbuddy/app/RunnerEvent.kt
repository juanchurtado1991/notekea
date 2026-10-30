package com.testbuddy.app

import com.testbuddy.CaseResult
import java.nio.file.Path
import kotlin.time.Duration.Companion.milliseconds

/**
 * What the automatic run in its own process tells the manager, one line each on its output: a case starts, it takes a
 * step, it ends. Lines start with [PREFIX], so the app's own output in between is ignored; tabs and line breaks in a
 * message are escaped.
 */
internal sealed class RunnerEvent {
    data class Started(val id: String) : RunnerEvent()

    data class Step(val text: String) : RunnerEvent()

    data class Ended(val result: CaseResult) : RunnerEvent()

    data object Done : RunnerEvent()

    fun line(): String = PREFIX + when (this) {
        is Started -> listOf(STARTED, id)
        is Step -> listOf(STEP, text)
        is Ended -> resultFields(result = result)
        Done -> listOf(DONE)
    }.joinToString(separator = SEPARATOR) { escape(it) }

    companion object {
        const val PREFIX = "TEST-BUDDY\t"

        fun parse(line: String): RunnerEvent? {
            if (!line.startsWith(PREFIX)) return null
            val fields = line.removePrefix(PREFIX).split(SEPARATOR).map(::unescape)
            return when (fields.firstOrNull()) {
                STARTED -> fields.getOrNull(1)?.let { Started(id = it) }
                STEP -> fields.getOrNull(1)?.let { Step(text = it) }
                ENDED -> parseResult(fields = fields.drop(n = 1))?.let { Ended(result = it) }
                DONE -> Done
                else -> null
            }
        }

        private fun resultFields(result: CaseResult): List<String> = when (result) {
            is CaseResult.Passed -> listOf(ENDED, result.id, PASSED, result.duration.inWholeMilliseconds.toString())

            is CaseResult.Skipped -> listOf(ENDED, result.id, SKIPPED)

            is CaseResult.Failed -> listOf(
                ENDED,
                result.id,
                FAILED,
                result.duration.inWholeMilliseconds.toString(),
                result.message,
                result.screenshot?.toString().orEmpty()
            ) + result.steps
        }

        private fun parseResult(fields: List<String>): CaseResult? {
            val id = fields.firstOrNull() ?: return null
            val duration = fields.getOrNull(2)?.toLongOrNull()?.milliseconds
            return when (fields.getOrNull(1)) {
                PASSED -> duration?.let { CaseResult.Passed(id = id, duration = it) }

                SKIPPED -> CaseResult.Skipped(id = id)

                FAILED -> duration?.let {
                    CaseResult.Failed(
                        id = id,
                        message = fields.getOrElse(3) { "" },
                        duration = it,
                        screenshot = fields.getOrNull(4)?.takeIf(String::isNotBlank)?.let(Path::of),
                        steps = fields.drop(n = 5)
                    )
                }

                else -> null
            }
        }

        private fun escape(text: String) = text.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")

        private fun unescape(text: String): String = buildString {
            var index = 0
            while (index < text.length) {
                val char = text[index]
                if (char == '\\' && index + 1 < text.length) {
                    append(
                        if (text[index + 1] == 't') {
                            '\t'
                        } else if (text[index + 1] == 'n') {
                            '\n'
                        } else {
                            text[index + 1]
                        }
                    )
                    index += 2
                } else {
                    append(char)
                    index++
                }
            }
        }

        private const val SEPARATOR = "\t"
        private const val STARTED = "started"
        private const val STEP = "step"
        private const val ENDED = "ended"
        private const val DONE = "done"
        private const val PASSED = "passed"
        private const val FAILED = "failed"
        private const val SKIPPED = "skipped"
    }
}

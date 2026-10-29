package com.testbuddy.app

import com.testbuddy.ManualAnswer
import java.nio.file.Files
import java.nio.file.Path

/** Keeps a tester's manual answers in [file], one per line, so they can close the Self-Test and go on later. */
internal class AnswerStore(private val file: Path) {
    fun load(): Map<String, ManualAnswer> {
        if (!Files.isRegularFile(file)) return emptyMap()
        return Files.readAllLines(file).mapNotNull(::parse).associateBy { it.id }
    }

    fun save(answers: Collection<ManualAnswer>) {
        Files.createDirectories(file.parent)
        Files.write(file, answers.map(::line))
    }

    private fun line(answer: ManualAnswer): String = when (answer) {
        is ManualAnswer.Passed -> listOf(answer.id, PASSED)
        is ManualAnswer.NotRun -> listOf(answer.id, NOT_RUN)
        is ManualAnswer.Failed -> listOf(answer.id, FAILED, escape(answer.note), answer.screenshot.orEmpty())
    }.joinToString(separator = SEPARATOR)

    private fun parse(line: String): ManualAnswer? {
        val fields = line.split(SEPARATOR)
        val id = fields.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
        return when (fields.getOrNull(1)) {
            PASSED -> ManualAnswer.Passed(id = id)

            NOT_RUN -> ManualAnswer.NotRun(id = id)

            FAILED -> ManualAnswer.Failed(
                id = id,
                note = unescape(fields.getOrElse(2) { "" }),
                screenshot = fields.getOrNull(3)?.takeIf { it.isNotBlank() }
            )

            else -> null
        }
    }

    private fun escape(text: String): String = text.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")

    private fun unescape(text: String): String = buildString {
        var index = 0
        while (index < text.length) {
            val char = text[index]
            if (char == '\\' && index + 1 < text.length) {
                append(
                    when (text[index + 1]) {
                        't' -> '\t'
                        'n' -> '\n'
                        else -> text[index + 1]
                    }
                )
                index += 2
            } else {
                append(char)
                index++
            }
        }
    }

    private companion object {
        const val SEPARATOR = "\t"
        const val PASSED = "passed"
        const val FAILED = "failed"
        const val NOT_RUN = "not-run"
    }
}

package com.testbuddy.app

import com.testbuddy.CaseResult
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.milliseconds

/** Keeps the automatic results in [file], so an app closed after the automatic part still reports them. */
internal class ResultStore(private val file: Path) {
    fun load(): Map<String, CaseResult> {
        if (!Files.isRegularFile(file)) return emptyMap()
        return Files.readAllLines(file).mapNotNull(::parse).associateBy { it.id }
    }

    fun save(results: Collection<CaseResult>) {
        Files.createDirectories(file.parent)
        Files.write(file, results.map(::line))
    }

    private fun line(result: CaseResult): String = when (result) {
        is CaseResult.Passed -> listOf(result.id, PASSED, result.duration.inWholeMilliseconds.toString())

        is CaseResult.Skipped -> listOf(result.id, SKIPPED)

        is CaseResult.Failed -> listOf(
            result.id,
            FAILED,
            result.duration.inWholeMilliseconds.toString(),
            result.message.replace(Regex(pattern = "\\s+"), " "),
            result.screenshot?.toString().orEmpty()
        ) + result.steps.map { it.replace(Regex(pattern = "\\s+"), " ") }
    }.joinToString(separator = SEPARATOR)

    private fun parse(line: String): CaseResult? {
        val fields = line.split(SEPARATOR)
        val id = fields.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
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

    private companion object {
        const val SEPARATOR = "\t"
        const val PASSED = "passed"
        const val FAILED = "failed"
        const val SKIPPED = "skipped"
    }
}

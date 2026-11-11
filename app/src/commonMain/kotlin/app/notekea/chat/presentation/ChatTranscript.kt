package app.notekea.chat.presentation

import app.notekea.chat.domain.ActionStep
import app.notekea.chat.domain.AnswerSource

/**
 * The chat as Markdown for Copy chat: every question, how it ended, the answer, the passages it read and what an
 * action planned and did, so a wrong answer can be reported with all it was given. A report, in English like the
 * crash report, not the chat's own words.
 */
internal object ChatTranscript {
    private const val SNIPPET_CHARS = 200

    fun of(state: ChatState): String = buildString {
        appendLine("# Notekea chat")
        appendLine()

        val model = state
            .tier
            ?.name
            ?.lowercase() ?: if (state.isSmall) "small" else "medium or large"

        appendLine("Model: $model · Mode: ${state.mode.name.lowercase()}")

        state.entries.forEach { entry ->
            appendLine()
            appendLine("## ${entry.question}")
            appendLine()
            appendLine("Scope: ${entry.scope.name.lowercase()}${entry.scopeName?.let { " ($it)" }.orEmpty()}")
            appendLine("Outcome: ${entry.outcome?.name?.lowercase() ?: "answering"}")
            entry.answeredWith?.let { appendLine("Answered with: ${it.name.lowercase()}") }
            entry.error?.let { appendLine("Error: $it") }
            entry.moves.forEach { appendLine("Step: ${it.move} ${it.target}") }

            if (entry.answer.isNotBlank()) {
                appendLine()
                appendLine(entry.answer)
            }

            sources(sources = entry.sources)
            entry.action?.let { action(action = it) }
        }
    }

    private fun StringBuilder.sources(sources: List<AnswerSource>) {
        if (sources.isEmpty()) return

        appendLine()
        appendLine("Sources:")

        sources.forEach { source ->
            val place = (listOf(source.note.value) + source.headingPath.drop(n = 1))
                .joinToString(separator = " › ")

            val snippet = source
                .text
                .replace(
                    oldChar = '\n',
                    newChar = ' '
                )
                .take(n = SNIPPET_CHARS)

            appendLine("[${source.number}] $place: $snippet")
        }
    }

    private fun StringBuilder.action(action: ChatAction) {
        appendLine()
        appendLine("Action: ${action.status.name.lowercase()}")
        action.problem?.let { appendLine("Problem: $it") }

        (action.shown + action.steps).forEach { appendLine("- ${stepOf(step = it)}") }

        action.found.forEach { found ->
            appendLine("Found ${found.rows.size}${found.query?.let { " with $it" }.orEmpty()}:")

            found.rows.forEach { row ->
                appendLine("  - ${row.note.value} ${row.cells.joinToString(separator = " · ")}")
            }
        }

        action.done?.let {
            appendLine("Done: created ${it.created}, changed ${it.changed}, moved ${it.moved}")
        }
    }

    private fun stepOf(step: ActionStep): String = listOfNotNull(
        step.kind.written,
        step
            .notes
            .takeIf { it.isNotEmpty() }
            ?.joinToString { it.value },
        step
            .folder
            ?.value
            ?.let { "folder=$it" },
        step
            .target
            ?.value
            ?.let { "target=$it" },
        step.title?.let { "title=$it" },
        step.property?.let { "$it=${step.value}" },
        step.tag?.let { "#$it" },
        step.period?.let { "period=$it" },
        step.text?.let { "text=${it.take(n = SNIPPET_CHARS)}" },
        step
            .content
            .takeIf { it.isNotEmpty() }
            ?.joinToString { "writes ${it.path.value}" }
    ).joinToString(separator = " ")
}

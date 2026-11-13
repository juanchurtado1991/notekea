package app.notekea.chat.domain.answer

import app.notekea.chat.domain.language.inLanguage
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A question the plan answered with tables of notes ("how many books did I finish, and which one did I like most?")
 * answered in words from those [tables], above them (the chat probe, 2026-11-11: it showed tables and said nothing).
 */
class AnswerFromFoundUseCase(private val llmEngine: LlmEngine) {
    operator fun invoke(
        question: String,
        tables: List<FoundNotes>,
        notes: Collection<NoteEntry> = emptyList(),
        language: String? = null,
        today: String? = null
    ): Flow<AnswerStep> = streamedReply(
        llmEngine = llmEngine,
        messages = listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = INSTRUCTIONS + today?.let { " Today is $it: a task dated before it is overdue." }.orEmpty()
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "${described(tables = tables, notes = notes)}\n\nQuestion: $question"
            )
        ).inLanguage(language = language),
        options = GenerationOptions(
            maxTokens = MAX_TOKENS,
            temperature = 0f
        )
    ).map { withoutTables(step = it) }

    // Large wrote the tables again under its answer, though told not to (the comparison with Claude, 2026-11-11).
    private fun withoutTables(step: AnswerStep): AnswerStep = when (step) {
        is AnswerStep.Writing -> step.copy(text = withoutTables(text = step.text))

        is AnswerStep.Done -> {
            val text = withoutTables(text = step.answer.text)
            step.copy(answer = step.answer.copy(text = text))
        }

        else -> step
    }

    // The text before a table written again, as "Table 1 (…)" or as Markdown rows.
    private fun withoutTables(text: String): String = text
        .substringBefore(delimiter = TABLE_ECHO)
        .lines()
        .takeWhile {
            !it.trimStart().startsWith(prefix = "|")
        }
        .joinToString(separator = "\n")
        .trim()

    // Each row with all its note's properties, not only the table's columns: "and which one did I like most?" asked
    // of a table of finished books reads their rating (the chat probe, 2026-11-11).
    private fun described(
        tables: List<FoundNotes>,
        notes: Collection<NoteEntry>
    ): String = tables
        .mapIndexed { index, table ->
            val kind = when {
                !table.isTasks -> "notes"
                table.period != null -> "tasks ${table.period}, every one of them"
                else -> "open tasks"
            }

            val columns =
                if (table.isTasks) " | task" else table.columns.joinToString(separator = "") { " | $it" }

            val header = "Table ${index + 1} (${table.rows.size} $kind): note$columns"

            // A task's note's properties are not the task's: a project's due date read as its task's.
            val described = if (table.isTasks) emptyList() else notes

            val rows = table
                .rows
                .take(n = MAX_ROWS)
                .joinToString(separator = "\n") {
                    rowOf(
                        row = it,
                        notes = described
                    )
                }

            "$header\n${rows.ifEmpty { "(no notes)" }}"
        }
        .joinToString(separator = "\n\n")

    private fun rowOf(
        row: FoundRow,
        notes: Collection<NoteEntry>
    ): String {
        val properties = notes
            .firstOrNull {
                it.file.path == row.note
            }
            ?.let { NoteProperties.of(note = it) }
            .orEmpty()

        val cells = row.cells.joinToString(separator = "") { " | $it" }

        val more =
            if (properties.isEmpty()) "" else " (${properties.joinToString(separator = "; ")})"

        val due = row
            .due
            ?.let { " ($it)" }
            .orEmpty()

        return "- ${row.note.nameWithoutExtension}$cells$due$more"
    }

    private companion object {
        const val MAX_TOKENS = 220
        const val MAX_ROWS = 40
        const val TABLE_ECHO = "\nTable "
        const val INSTRUCTIONS =
            "You are the assistant of Notekea, a notes app. Answer the question in one or two sentences from the " +
                "tables of the person's notes below, which the app found for it: count the rows when asked how " +
                "many, and name the notes. If the tables do not answer it, say so plainly; if they disagree, say " +
                "so. The tables are shown below your answer, so do not repeat them."
    }
}

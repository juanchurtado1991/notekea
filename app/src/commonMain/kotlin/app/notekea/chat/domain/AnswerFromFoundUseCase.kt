package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import kotlinx.coroutines.flow.Flow

/**
 * A question the plan answered with tables of notes ("how many books did I finish, and which one did I like most?")
 * answered in words from those [tables], above them (the chat probe, 2026-11-11: it showed tables and said nothing).
 */
class AnswerFromFoundUseCase(private val llmEngine: LlmEngine) {
    operator fun invoke(
        question: String,
        tables: List<FoundNotes>,
        language: String? = null
    ): Flow<AnswerStep> = streamedReply(
        llmEngine = llmEngine,
        messages = listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = INSTRUCTIONS
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "${described(tables = tables)}\n\nQuestion: $question"
            )
        ).inLanguage(language = language),
        options = GenerationOptions(
            maxTokens = MAX_TOKENS,
            temperature = 0f
        )
    )

    private fun described(tables: List<FoundNotes>): String = tables
        .mapIndexed { index, table ->
            val header = "Table ${index + 1} (${table.rows.size} notes): note" +
                table.columns.joinToString(separator = "") { " | $it" }

            val rows = table
                .rows
                .take(n = MAX_ROWS)
                .joinToString(separator = "\n") { row ->
                    "- ${row.note.nameWithoutExtension}" + row.cells.joinToString(separator = "") { " | $it" }
                }

            "$header\n${rows.ifEmpty { "(no notes)" }}"
        }
        .joinToString(separator = "\n\n")

    private companion object {
        const val MAX_TOKENS = 220
        const val MAX_ROWS = 40
        const val INSTRUCTIONS =
            "You are the assistant of Notekea, a notes app. Answer the question in one or two sentences from the " +
                "tables of the person's notes below, which the app found for it: count the rows when asked how " +
                "many, and name the notes. If the tables do not answer it, say so plainly. The tables are shown " +
                "below your answer, so do not repeat them."
    }
}

package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.Outcome

/** The passages of the notes that match some words, and the notes whose properties hold one of them. */
class SearchTool(private val answerFromNotes: AnswerFromNotesUseCase) : ChatTool {
    override val name = NAME
    override val description = "words to look for in the notes; you get the passages that match."
    override val arguments = listOf(ToolArgument(name = QUERY))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = QUERY)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val query = call.text(name = QUERY)

        val passages = (
            answerFromNotes.excerpts(context.vaultPath, context.notes, query) as? Outcome.Success
            )
            ?.value
            .orEmpty()

        val lines = passages.take(n = PASSAGES).map {
            "- ${it.note.nameWithoutExtension}: ${it.text.take(n = PASSAGE_CHARS).replace('\n', ' ')}"
        } + propertiesMatching(
            query = query,
            context = context
        )

        if (lines.isEmpty()) return ToolResult(forModel = "search \"$query\": nothing found.")

        return ToolResult(forModel = "search \"$query\":\n" + lines.joinToString(separator = "\n"))
    }

    // The index reads a note's text, not its frontmatter: notes whose property values hold a word of the query.
    private fun propertiesMatching(
        query: String,
        context: ToolContext
    ): List<String> {
        val words = query
            .lowercase()
            .split(' ', ',', '.', '?')
            .filter {
                it.length >= MIN_WORD
            }

        return context
            .notes
            .filter { note ->
                val values = NoteProperties
                    .of(note = note)
                    .joinToString(separator = "\n")
                    .lowercase()

                words.any {
                    it in values
                }
            }
            .take(n = PROPERTY_NOTES)
            .map { note ->
                val written = NoteProperties.of(note = note).joinToString(separator = "; ")

                "- ${note.file.path.nameWithoutExtension} (properties: $written)"
            }
    }

    companion object {
        const val NAME = "search"
        const val QUERY = "query"
        private const val PASSAGES = 4
        private const val MIN_WORD = 3
        private const val PROPERTY_NOTES = 8
        private const val PASSAGE_CHARS = 400
    }
}

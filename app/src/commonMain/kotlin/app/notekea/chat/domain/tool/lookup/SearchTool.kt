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
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath

/**
 * The passages of the notes that match some words, the notes whose properties hold one of them, and, with a
 * [noteTextCache], every note whose text writes them all: "tag the journal notes that mention the garden".
 */
class SearchTool(
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val noteTextCache: NoteTextCache? = null
) : ChatTool {
    override val name = NAME
    override val description =
        "words to look for in the notes; you get the passages that match and every note that writes them."
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
        ) + writing(
            query = query,
            context = context
        )

        if (lines.isEmpty()) return ToolResult(forModel = "search \"$query\": nothing found.")

        return ToolResult(forModel = "search \"$query\":\n" + lines.joinToString(separator = "\n"))
    }

    // The passages are the best few: a task about every note that mentions a word needs them all, by name (the
    // loop's fifth run tagged all six journal notes for "the ones that mention the garden" from four passages).
    private suspend fun writing(
        query: String,
        context: ToolContext
    ): List<String> {
        val cache = noteTextCache ?: return emptyList()
        val words = wordsOf(query = query)
        if (words.isEmpty()) return emptyList()

        val names = cache
            .textsOf(context.vaultPath, context.notes.filter { it.file.path.isMarkdown })
            .filterValues { text ->
                val body = text.body.lowercase()

                words.all {
                    it in body
                }
            }
            .keys
            .map { it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION) }
            .sorted()

        if (names.isEmpty()) return emptyList()

        val shown = names.take(n = WRITING_NOTES).joinToString()

        val more = (names.size - WRITING_NOTES)
            .takeIf {
                it > 0
            }
            ?.let { ", and $it more" }
            .orEmpty()

        val written = words.joinToString(separator = " ")

        return listOf("Every note that writes \"$written\" (${names.size}): $shown$more")
    }

    // The code review of 2026-11-22: split at spaces, commas, stops and "?" only, a word kept its
    // quotes, its brackets or its "¿", and matched nothing.
    private fun wordsOf(query: String): List<String> = query
        .lowercase()
        .split(regex = NOT_WORD)
        .filter {
            it.length >= MIN_WORD
        }

    // The index reads a note's text, not its frontmatter: notes whose property values hold a word of the query.
    private fun propertiesMatching(
        query: String,
        context: ToolContext
    ): List<String> {
        val words = wordsOf(query = query)

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
        private const val WRITING_NOTES = 40
        private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")
    }
}

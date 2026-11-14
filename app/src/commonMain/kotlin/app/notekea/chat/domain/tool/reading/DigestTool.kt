package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.plan.TextParts
import app.notekea.chat.domain.prompt.WritePrompt
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteTextCache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * What long notes or documents say about a question, read part by part in calls of their own: the heavy reading
 * happens outside the turn's context, which gets only what matters of each part (`docs/agent-loop.md`, step 4).
 */
class DigestTool(
    private val llmEngine: LlmEngine,
    noteTextCache: NoteTextCache
) : ChatTool {
    private val opener = NoteOpener(noteTextCache = noteTextCache)

    override val name = NAME

    override val description =
        "the names of long notes or documents and a question; you get what each says about it, read to its end."

    override val arguments = listOf(
        ToolArgument(
            name = NOTES,
            isList = true
        ),
        ToolArgument(name = QUESTION)
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.list(name = NOTES).joinToString {
        context.pathOf(name = it) ?: it
    }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val question = call.text(name = QUESTION).ifEmpty { context.task }
        val names = call.list(name = NOTES).take(n = MAX_NOTES)
        if (names.isEmpty()) return ToolResult(forModel = "digest: name the notes to read.")

        val opened = names.map { name ->
            name to opener.open(
                name = name,
                context = context
            )
        }

        val parts = (MAX_PARTS / names.size).coerceAtLeast(minimumValue = 1)

        val digests = opened.map { (name, note) ->
            when {
                note == null -> "digest \"$name\": no note of that name."

                note.read.text.isBlank() -> "digest \"${note.shown}\": it holds no text."

                else -> digestOf(
                    note = note,
                    question = question,
                    parts = parts
                )
            }
        }

        return ToolResult(forModel = digests.joinToString(separator = "\n\n"))
    }

    private suspend fun digestOf(
        note: OpenedNote,
        question: String,
        parts: Int
    ): String = coroutineScope {
        val all = TextParts.of(text = note.read.text)

        val said = all
            .take(n = parts)
            .map { part ->
                async {
                    saidOf(
                        note = note,
                        question = question,
                        part = part
                    )
                }
            }
            .awaitAll()
            .filter { it.isNotBlank() }

        val unread = (all.size - parts)
            .takeIf {
                it > 0
            }
            ?.let { "\n(Its last $it parts were not read: digest it alone to read them.)" }
            .orEmpty()

        "digest \"${note.shown}\" (${all.size.coerceAtMost(maximumValue = parts)} parts read):\n" +
            said.joinToString(separator = "\n") + unread
    }

    private suspend fun saidOf(
        note: OpenedNote,
        question: String,
        part: String
    ): String {
        val text = StringBuilder()

        llmEngine
            .generate(
                messages = WritePrompt.digest(
                    request = question,
                    name = note.shown,
                    text = part
                ),
                options = GenerationOptions(
                    maxTokens = DIGEST_TOKENS,
                    temperature = TEMPERATURE
                )
            )
            .collect { text.append(it) }

        return text.toString().trim()
    }

    companion object {
        const val NAME = "digest"
        const val NOTES = "notes"
        const val QUESTION = "question"
        private const val MAX_NOTES = 6
        private const val MAX_PARTS = 12
        private const val DIGEST_TOKENS = 220
        private const val TEMPERATURE = 0.3f
    }
}

package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteTextCache

/**
 * A note's headings and how long each part is, with the documents attached to it: what a long note holds, seen
 * before reading any of it, so only the part that matters is read (`docs/agent-loop.md`, step 4).
 */
class OutlineTool(noteTextCache: NoteTextCache) : ChatTool {
    private val opener = NoteOpener(noteTextCache = noteTextCache)

    override val name = NAME

    override val description =
        "a note's name; you get its headings and how many words each part holds, to read only the part needed."

    override val arguments = listOf(ToolArgument(name = NOTE))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String {
        val named = call.text(name = NOTE)

        return context.pathOf(name = named) ?: named
    }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val name = call.text(name = NOTE)

        val note = opener.open(
            name = name,
            context = context
        ) ?: return ToolResult(forModel = "outline \"$name\": no note of that name.")

        val text = note.read.text
        if (text.isBlank()) return ToolResult(forModel = "outline \"${note.shown}\": it holds no text.")

        val parts = TextOutline.of(text = text)

        val lines = parts
            .joinToString(separator = "\n") { part ->
                val words = TextOutline.wordsIn(text = part.textIn(text = text))
                val indent = "  ".repeat(n = (part.level - 1).coerceAtLeast(minimumValue = 0))
                val heading = part.heading.ifEmpty { BEFORE }

                "$indent- $heading ($words words)"
            }

        val with = note
            .read
            .attached
            .takeIf { it.isNotEmpty() }
            ?.let { ", with its attached ${it.joinToString()}" }
            .orEmpty()

        return ToolResult(
            forModel = "outline \"${note.shown}\" (${TextOutline.wordsIn(text = text)} words$with):\n$lines"
        )
    }

    companion object {
        const val NAME = "outline"
        const val NOTE = "note"
        private const val BEFORE = "(before the first heading)"
    }
}

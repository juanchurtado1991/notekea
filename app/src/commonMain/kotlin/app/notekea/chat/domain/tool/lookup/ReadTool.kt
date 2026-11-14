package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.AttachedTexts
import app.notekea.chat.domain.plan.NoteProperties
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.chat.domain.tool.reading.TextOutline
import app.notekea.core.notes.domain.NoteTextCache

/**
 * A note's text, with the text of the documents attached to it (a pasted PDF): what the turn read, and so what a
 * note it writes is written from. A note with no text to work from is said, never written from.
 */
class ReadTool(noteTextCache: NoteTextCache) : ChatTool {
    private val attachedTexts = AttachedTexts(noteTextCache = noteTextCache)

    override val name = NAME

    override val description =
        "a note's name; you get its text, with the text of the documents attached to it (a pasted PDF). " +
            "Leave section out to read it whole; with a heading of its outline, only that part of a long note."

    override val arguments = listOf(
        ToolArgument(name = NOTE),
        ToolArgument(
            name = SECTION,
            isRequired = false
        )
    )

    override val effect = ToolEffect.READS

    // A note read is listed by its path in the vault ("Read Recipes/Arepas"), however the model named it.
    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String {
        val named = call.text(name = NOTE)

        // A section is another read: the same note read by two of its sections is not a call made twice.
        val section = call
            .text(name = SECTION)
            .takeIf { it.isNotEmpty() }
            ?.let { " › $it" }
            .orEmpty()

        return (context.pathOf(name = named) ?: named) + section
    }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val name = call.text(name = NOTE)
        val missing = ToolResult(forModel = "read \"$name\": no note of that name.")
        val path = context.noteNamed(name = name) ?: return missing

        // The open tab is no note when it is a folder or a view: it reads as a name nothing matches.
        val note = context.notes.firstOrNull {
            it.file.path == path
        } ?: return missing

        val read = attachedTexts.of(
            vaultPath = context.vaultPath,
            note = note,
            notes = context.notes
        )

        val shown = path.nameWithoutExtension

        val unread = read
            .unread
            .takeIf { it.isNotEmpty() }
            ?.let { " It attaches ${it.joinToString()}, which holds no text that can be read." }
            .orEmpty()

        if (read.text.isBlank()) {
            context.reads.addEmpty(name = shown)

            return ToolResult(forModel = "read \"$shown\": $NO_TEXT$unread")
        }

        context.reads.add(note = path)
        val section = call.text(name = SECTION)

        if (section.isNotEmpty()) {
            return sectionOf(
                text = read.text,
                section = section,
                shown = shown
            )
        }

        val properties = NoteProperties.of(note = note).joinToString(separator = "") { "$it\n" }

        // Counted here, not by the model: "how many words does Arepas have?" got 109, 128 and 63 (the chat probe).
        val words = WORDS.findAll(input = read.text).count()

        val more = if (read.text.length > NOTE_CHARS) {
            "\n(Its first $NOTE_CHARS of ${read.text.length} characters. A write_from_notes step that names " +
                "this note reads all of it.)"
        } else {
            ""
        }

        val with = read
            .attached
            .takeIf { it.isNotEmpty() }
            ?.let { ", with its attached ${it.joinToString()}" }
            .orEmpty()

        return ToolResult(
            forModel = "read \"$shown\" ($words words$with):\n$properties" +
                "${read.text.take(n = NOTE_CHARS)}$more$unread"
        )
    }

    // One part of a long note, by its heading, its subheadings with it; a heading the note lacks gets its headings.
    private fun sectionOf(
        text: String,
        section: String,
        shown: String
    ): ToolResult {
        val parts = TextOutline.of(text = text)

        val part = TextOutline.find(
            parts = parts,
            section = section
        )

        if (part == null) {
            val headings = parts
                .filter {
                    it.level > 0
                }
                .joinToString { it.heading }
                .ifEmpty { "none" }

            return ToolResult(
                forModel = "read \"$shown\": no section \"$section\". Its headings: $headings."
            )
        }

        val whole = part.textIn(text = text)

        val more = if (whole.length > SECTION_CHARS) {
            "\n(Its first $SECTION_CHARS of ${whole.length} characters: read one of its subsections for the rest.)"
        } else {
            ""
        }

        return ToolResult(
            forModel = "read \"$shown\", section \"${part.heading}\" " +
                "(${TextOutline.wordsIn(text = whole)} words):\n${whole.take(n = SECTION_CHARS)}$more"
        )
    }

    companion object {
        const val NAME = "read"
        const val NOTE = "note"
        const val SECTION = "section"
        private const val SECTION_CHARS = 6_000
        private val WORDS = Regex(pattern = "[\\p{L}\\p{N}]+(['’-][\\p{L}\\p{N}]+)*")
        private const val NO_TEXT = "nothing found: this note holds no text to work from."
        private const val NOTE_CHARS = 2_500
    }
}

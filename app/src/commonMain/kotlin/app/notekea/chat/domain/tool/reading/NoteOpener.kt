package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.plan.AttachedTexts
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteTextCache

/**
 * A note opened by the name the model gave it, with the text of the documents attached to it; what a turn reads this
 * way is what a note it writes is written from ([ToolContext.reads]).
 */
internal class NoteOpener(noteTextCache: NoteTextCache) {
    private val attachedTexts = AttachedTexts(noteTextCache = noteTextCache)

    /** The note [name] names, noted as read; null when the vault has none or the name is a folder or a view. */
    suspend fun open(
        name: String,
        context: ToolContext
    ): OpenedNote? {
        val path = context.noteNamed(name = name) ?: return null

        val note = context.notes.firstOrNull {
            it.file.path == path
        } ?: return null

        val read = attachedTexts.of(
            vaultPath = context.vaultPath,
            note = note,
            notes = context.notes
        )

        val shown = path.nameWithoutExtension
        val reads = context.reads
        if (read.text.isBlank()) reads.addEmpty(name = shown) else reads.add(note = path)

        return OpenedNote(
            path = path,
            shown = shown,
            read = read
        )
    }
}

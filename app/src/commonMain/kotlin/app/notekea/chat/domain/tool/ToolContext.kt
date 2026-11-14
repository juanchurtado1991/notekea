package app.notekea.chat.domain.tool

import app.notekea.chat.domain.plan.VaultNames
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * What the tools of one turn work on: the vault and its [notes], the note open in the window, the [task] as the
 * person wrote it (a web page is read only from an address it writes) and what the turn has read ([reads]). The
 * vault's names are indexed once for the turn, not at every call.
 */
class ToolContext(
    val vaultPath: String,
    val notes: Collection<NoteEntry>,
    val task: String,
    openNote: RelativePath?
) {
    val reads = TurnReads()

    private val names = VaultNames(
        notes = notes,
        openNote = openNote
    )

    /** The note [name] names (its name, its path, "this note"); null when the vault has none. */
    fun noteNamed(name: String): RelativePath? = runCatching {
        names.noteOf(name = name)
    }.getOrNull()

    /** The vault's path of the note [name] names, without its extension; null when no note has that name. */
    fun pathOf(name: String): String? = noteNamed(name = name)
        ?.value
        ?.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
}

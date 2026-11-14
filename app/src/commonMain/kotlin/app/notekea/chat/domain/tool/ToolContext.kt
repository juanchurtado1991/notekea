package app.notekea.chat.domain.tool

import app.notekea.chat.domain.plan.VaultNames
import app.notekea.chat.domain.tool.work.TurnWork
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * What the tools of one turn work on: the vault and its [notes], the note open in the window, the [task] as the
 * person wrote it (a web page is read only from an address it writes), [today], what the turn has read ([reads])
 * and its working memory ([work]). The vault's names are indexed once for the turn.
 */
class ToolContext(
    val vaultPath: String,
    val notes: Collection<NoteEntry>,
    val task: String,
    openNote: RelativePath?,
    val today: CalendarDate? = null
) {
    val reads = TurnReads()
    val work = TurnWork()

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

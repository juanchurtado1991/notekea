package app.notekea.chat.domain.tool

import app.notekea.chat.domain.plan.VaultNames
import app.notekea.chat.domain.tool.work.TurnWork
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.SourceTrust

/**
 * What one turn's tools work on: the vault, its [notes], the open note, the [task] as written, [today], where a web
 * search is made ([country]) and whose sites it [trust]s, the notes the memory says the task means ([remembered]),
 * and what the turn read ([reads]), found on the web ([web]) and works on ([work]).
 */
class ToolContext(
    val vaultPath: String,
    val notes: Collection<NoteEntry>,
    val task: String,
    val openNote: RelativePath?,
    val today: CalendarDate? = null,
    val country: String? = null,
    val trust: SourceTrust = SourceTrust(
        sources = emptyList(),
        memory = emptyList()
    ),
    val remembered: Set<RelativePath> = emptySet()
) {
    val reads = TurnReads()
    val work = TurnWork()
    val web = TurnWeb()

    private val names = VaultNames(
        notes = notes,
        openNote = openNote
    )

    /** The note [name] names (its name, its path, "this note"); null when the vault has none. */
    fun noteNamed(name: String): RelativePath? = runCatching {
        names.noteOf(name = name)
    }.getOrNull()

    /** The notes that share the bare name [name], by their paths: more than one is a name the person must settle. */
    fun sharing(name: String): List<String> {
        val bare = name
            .trim()
            .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
            .lowercase()

        if ('/' in bare) return emptyList()

        return notes
            .map { it.file.path }
            .filter {
                it.isMarkdown && it.nameWithoutExtension.lowercase() == bare
            }
            .map { it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION) }
            .takeIf {
                it.size > 1
            }
            .orEmpty()
    }

    /** The vault's path of the note [name] names, without its extension; null when no note has that name. */
    fun pathOf(name: String): String? = noteNamed(name = name)
        ?.value
        ?.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
}

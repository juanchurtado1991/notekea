package app.notekea.chat.domain.tool

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.VaultNames
import app.notekea.chat.domain.tool.work.TurnWork
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.SourceTrust

/**
 * What one turn's tools work on: the vault, its [notes], the [task], what it read, found and works on. A question of
 * the open note or its folder has those notes as its [scope]: the looking tools see only them ([looked]), the changing
 * ones the whole vault ("move this note to Archive"). The web's country and trust, and the [frame], are said apart.
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
    val frame: TaskFrame? = null,
    val scope: Set<RelativePath>? = null
) {
    /** The notes the looking tools see: the [scope]'s, or the whole vault's. */
    val looked: Collection<NoteEntry> = scope
        ?.let { only ->
            notes.filter {
                it.file.path in only
            }
        }
        ?: notes

    val reads = TurnReads()
    val work = TurnWork()
    val web = TurnWeb()

    /** The tables the turn's finding calls made, in order: shown under its answer, each row opening its note. */
    val tables = mutableListOf<FoundNotes>()

    /** The passages the turn's searches gave the model: an answer's sources, with the notes it read. */
    val passages = mutableListOf<AnswerSource>()

    private val names = VaultNames(
        notes = notes,
        openNote = openNote
    )

    /** The note [name] names (its name, its path, "this note"); null when the vault has none. */
    fun noteNamed(name: String): RelativePath? = runCatching {
        names.noteOf(name = name)
    }.getOrNull()

    /** The note [name] names when it is one the looking tools see ([looked]); null otherwise. */
    fun lookedNamed(name: String): RelativePath? = noteNamed(name = name)?.takeIf {
        scope == null || it in scope
    }

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
            .map { it.bare() }
            .takeIf {
                it.size > 1
            }
            .orEmpty()
    }

    /** The vault's path of the note [name] names, without its extension; null when no note has that name. */
    fun pathOf(name: String): String? = noteNamed(name = name)
        ?.bare()
}

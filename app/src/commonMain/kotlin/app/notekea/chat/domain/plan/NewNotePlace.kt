package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * Where a note the chat creates goes: shown on its card and chosen there before Do it (the developer's smoke of
 * 2026-11-14: a note was created at the vault's root without a word about where).
 */
object NewNotePlace {
    private val PLACED = setOf(ActionKind.CREATE_NOTE, ActionKind.WRITE_FROM_NOTES)

    /** Whether [step] creates one note whose folder can be chosen. */
    fun isChosenFor(step: ActionStep): Boolean = step.kind in PLACED && step.content.size <= 1

    /** The folder [step]'s note will be created in. */
    fun of(step: ActionStep): RelativePath = step
        .content
        .firstOrNull()
        ?.path
        ?.parent
        ?: step.folder
        ?: RelativePath.ROOT

    /** [step] creating its note in [folder] instead. */
    fun placed(
        step: ActionStep,
        folder: RelativePath
    ): ActionStep = step.copy(
        folder = folder,
        content = step.content.map { it.copy(path = folder.child(name = it.path.name)) }
    )

    /** The vault's root, then every folder that holds one of [notes], by path. */
    fun foldersOf(notes: Collection<NoteEntry>): List<RelativePath> {
        val folders = sortedSetOf<String>()

        notes.forEach { note ->
            var folder = note.file.path.parent

            while (folder != null &&
                !folder.isRoot &&
                folders.add(folder.value)
            ) {
                folder = folder.parent
            }
        }

        return listOf(RelativePath.ROOT) + folders.map { RelativePath(value = it) }
    }
}

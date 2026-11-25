package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.NewNotePlace
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.core.notes.domain.NoteEntry

/**
 * A new note's title written with its folder ("Diary/2026-11-11") as the title and the folder it says: a "/" is left
 * out of a file's name, so the note was created as "Diary2026-11-11" and no later step found it (dev, 2026-11-24).
 */
internal object NewNoteTitle {
    private val TITLED = setOf(ActionKind.CREATE_NOTE, ActionKind.WRITE_FROM_NOTES)
    private const val SEPARATOR = '/'

    fun placed(
        step: PlannedStep,
        notes: Collection<NoteEntry>
    ): PlannedStep {
        val title = step[ActionField.TITLE]?.trim(SEPARATOR).orEmpty()
        val isPath = step.kind in TITLED && SEPARATOR in title
        if (!isPath) return step

        val folder = title.substringBeforeLast(delimiter = SEPARATOR).trim()
        val name = title.substringAfterLast(delimiter = SEPARATOR).trim()
        val said = step[ActionField.FOLDER]?.trim(SEPARATOR)

        // A folder that says another place is the one asked for, and "AC/DC tabs" is in no folder of the vault.
        val isFolder = if (said != null) {
            said.endsWith(
                suffix = folder,
                ignoreCase = true
            )
        } else {
            NewNotePlace.foldersOf(notes = notes).any {
                it.value.equals(
                    other = folder,
                    ignoreCase = true
                )
            }
        }

        if (!isFolder) return step

        return step.copy(
            fields = step.fields +
                (ActionField.TITLE to name) +
                (ActionField.FOLDER to (said ?: folder))
        )
    }
}

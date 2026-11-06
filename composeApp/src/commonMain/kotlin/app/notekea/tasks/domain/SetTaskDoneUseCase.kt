package app.notekea.tasks.domain

import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.TaskFinder

/**
 * Checks or unchecks a task from the Tasks tab (`ROADMAP.md` 2.11), changing that one character of its note, as the
 * editor does. A note that no longer has the task where the index saw it, or changes meanwhile, is left alone. Returns
 * what was written, or null if nothing was.
 */
class SetTaskDoneUseCase(
    private val noteContentStore: NoteContentStore,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase
) {
    private val finder = TaskFinder()

    suspend operator fun invoke(
        vaultPath: String,
        row: TaskRow
    ): NoteRewrite? {
        val original = noteContentStore.read(vaultPath = vaultPath, path = row.path) ?: return null
        val document = NoteDocument.fromText(text = original)
        val marker = finder.tasks(text = document.body).firstOrNull {
            it.stateIndex == row.task.stateIndex && it.isDone == row.task.isDone
        } ?: return null
        val body = document.body.replaceRange(marker.stateIndex, marker.stateIndex + 1, marker.toggledState.toString())
        val rewrite = NoteRewrite(
            path = row.path,
            original = original,
            updated = document.copy(body = body).toText(),
            linkCount = 0
        )
        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }
}

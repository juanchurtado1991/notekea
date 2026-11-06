package app.notekea.tasks.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.TaskFinder

/**
 * Checks or unchecks a task from the Tasks tab (`ROADMAP.md` 2.11), changing that one character of its note, as the
 * editor does: to the opposite of what [TaskRow] shows, so a second click before the list changes asks for nothing new.
 * A note that no longer has that line where the index saw it, or changes meanwhile, is left alone. Returns what was
 * written, or null if nothing was.
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
        val text = document.body
        val marker = finder.tasks(text = text).firstOrNull { it.stateIndex == row.task.stateIndex } ?: return null
        val lineEnd = text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = marker.stateIndex)
        if ((lineEnd.takeIf { it >= 0 } ?: text.length) != row.task.endIndex) return null
        val isAsked = marker.isDone != row.task.isDone
        val state = marker.toggledState.toString()
        val body = if (isAsked) text else text.replaceRange(marker.stateIndex, marker.stateIndex + 1, state)
        val rewrite = NoteRewrite(
            path = row.path,
            original = original,
            updated = document.copy(body = body).toText(),
            linkCount = 0
        )
        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }
}

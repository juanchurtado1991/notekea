package app.notekea.editor.presentation.viewmodel

import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.model.EditorIntent
import kotlinx.coroutines.CompletableDeferred

/**
 * Waits until every editor but [editor] that still holds [path] has saved it, for [editor] to open it next: a tab moved
 * to the other pane would otherwise read the file before the last keystrokes reach it, and its next save would be a
 * false conflict.
 */
internal suspend fun saveInOtherEditors(
    path: RelativePath,
    editor: EditorViewModel,
    editors: List<EditorViewModel>
) {
    editors.filter {
        it !== editor && it.state.value.notePath == path
    }.forEach { other ->
        val saved = CompletableDeferred<Unit>()
        other.onIntent(intent = EditorIntent.OnHandOver(done = saved))
        saved.await()
    }
}

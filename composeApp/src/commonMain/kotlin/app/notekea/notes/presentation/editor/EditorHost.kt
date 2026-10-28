package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * What the editor's parts (find, text edits, files, note info, saving, properties, the file on disk, templates) share
 * with [EditorViewModel], which routes the intents to them: the [state] and the text [buffer], and the only ways to
 * change the state ([reduce]), send an effect ([emit]) and start work ([launch], whose failures the ViewModel shows).
 */
internal interface EditorHost {
    val state: StateFlow<EditorState>
    val buffer: TextFieldState

    fun reduce(
        transform: (EditorState) -> EditorState
    )

    fun emit(
        effect: EditorEffect
    )

    fun launch(
        block: suspend () -> Unit
    ): Job
}

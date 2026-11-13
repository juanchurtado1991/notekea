package app.notekea.editor.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import app.notekea.core.presentation.ViewModelHost
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorState

/**
 * What the editor's parts (find, text edits, files, note info, saving, properties, the file on disk, templates) share
 * with [EditorViewModel], which routes the intents to them: the [state] and the text [buffer], and the only ways to
 * change the state ([reduce]), send an effect ([emit]) and start work ([launch], whose failures the ViewModel shows).
 */
internal interface EditorHost : ViewModelHost<EditorState, EditorEffect> {
    val buffer: TextFieldState
}

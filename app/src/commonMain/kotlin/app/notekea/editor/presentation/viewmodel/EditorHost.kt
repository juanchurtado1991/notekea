package app.notekea.editor.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import app.notekea.core.presentation.ViewModelHost
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorState

/**
 * What the editor's parts (find, text edits, files, note info, saving, properties, the file on disk, templates) share
 * with [EditorViewModel], which routes the intents to them: besides the [ViewModelHost], the text [buffer].
 */
internal interface EditorHost : ViewModelHost<EditorState, EditorEffect> {
    val buffer: TextFieldState
}

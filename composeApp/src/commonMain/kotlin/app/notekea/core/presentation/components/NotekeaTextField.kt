package app.notekea.core.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_ERROR = "Choose a name without characters like / or :"
private const val PREVIEW_INVALID = "a/b"
private const val PREVIEW_PLACEHOLDER = "Search notes…"
private const val PREVIEW_VALUE = "pricing"
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

/**
 * Single- or multi-line text input with an optional placeholder, styled with the Notekea theme. [errorText] shows a
 * validation message under the field, next to what it is about, while keeping the typed value. [onSubmit] runs on
 * Enter in a single-line field (handled as a key event, so it behaves the same on every desktop platform).
 */
@Composable
fun NotekeaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    errorText: String? = null,
    onSubmit: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onPreviewKeyEvent { event ->
            val isEnterDown = event.type == KeyEventType.KeyDown && event.key in SUBMIT_KEYS
            val isSubmit = singleLine && onSubmit != null && isEnterDown
            if (isSubmit) onSubmit()
            isSubmit
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = placeholder?.let { text -> { Text(text = text, style = MaterialTheme.typography.bodyMedium) } },
        supportingText = errorText?.let { text -> { Text(text = text, style = MaterialTheme.typography.bodySmall) } },
        isError = errorText != null,
        singleLine = singleLine,
        shape = MaterialTheme.shapes.small
    )
}

@Preview
@Composable
private fun NotekeaTextFieldPreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaTextField(value = "", onValueChange = {}, placeholder = PREVIEW_PLACEHOLDER)
            NotekeaTextField(value = PREVIEW_VALUE, onValueChange = {}, placeholder = PREVIEW_PLACEHOLDER)
            NotekeaTextField(value = PREVIEW_INVALID, onValueChange = {}, errorText = PREVIEW_ERROR)
        }
    }
}

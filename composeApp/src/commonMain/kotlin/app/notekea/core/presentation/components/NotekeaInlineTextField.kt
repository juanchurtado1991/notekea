package app.notekea.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val FIELD_VERTICAL_PADDING_DP = 1
private const val PREVIEW_NAME = "Tomato plan"
private const val PREVIEW_WIDTH_DP = 220
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

/**
 * Single-line field that edits a name in place (renaming in the tree). It takes focus and selects the whole text when
 * it appears, so typing replaces the name; Enter submits and Esc cancels. [onFocusLost] runs when the user clicks
 * elsewhere (renaming submits then, as file managers do). [isError] outlines the field in the error color; the message
 * is shown by the caller next to it. The text belongs to the caller; only the selection is kept here.
 */
@Composable
fun NotekeaInlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onFocusLost: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    var selection by remember { mutableStateOf(value = TextRange(start = 0, end = value.length)) }
    var hasHadFocus by remember { mutableStateOf(value = false) }
    LaunchedEffect(key1 = focusRequester) { focusRequester.requestFocus() }
    BasicTextField(
        value = TextFieldValue(text = value, selection = selection),
        onValueChange = {
            selection = it.selection
            if (it.text != value) onValueChange(it.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester = focusRequester)
            .onFocusChanged { state ->
                if (state.isFocused) hasHadFocus = true
                if (!state.isFocused && hasHadFocus) onFocusLost?.invoke()
            }.submitOrCancelOnKeys(onSubmit = onSubmit, onCancel = onCancel),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
        singleLine = true,
        cursorBrush = SolidColor(value = colors.primary),
        decorationBox = { field -> InlineFieldFrame(isError = isError, field = field) }
    )
}

@Composable
private fun InlineFieldFrame(
    isError: Boolean,
    field: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = if (isError) colors.error else colors.primary,
                shape = MaterialTheme.shapes.extraSmall
            ).padding(horizontal = NotekeaDimensions.SpacingXs, vertical = FIELD_VERTICAL_PADDING_DP.dp)
    ) { field() }
}

private fun Modifier.submitOrCancelOnKeys(
    onSubmit: () -> Unit,
    onCancel: () -> Unit
): Modifier = onPreviewKeyEvent { event ->
    val isKeyDown = event.type == KeyEventType.KeyDown
    val isSubmit = isKeyDown && event.key in SUBMIT_KEYS
    val isCancel = isKeyDown && event.key == Key.Escape
    if (isSubmit) onSubmit()
    if (isCancel) onCancel()
    isSubmit || isCancel
}

@Preview
@Composable
private fun NotekeaInlineTextFieldPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaInlineTextField(value = PREVIEW_NAME, onValueChange = {}, onSubmit = {}, onCancel = {})
        }
    }
}

@Preview
@Composable
private fun NotekeaInlineTextFieldErrorPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaInlineTextField(
                value = PREVIEW_NAME,
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                isError = true
            )
        }
    }
}

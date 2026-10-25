package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import kotlinx.coroutines.flow.drop

private const val PREVIEW_TITLE = "Tomato plan"
private const val PREVIEW_WIDTH_DP = 420
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

/**
 * A page title that edits in place (the note's name above the editor): it reads as a heading and shows a line under it
 * only while focused or refused. Enter or leaving the field commits the text through [onCommit] when it changed; Esc
 * puts [value] back. [onEdit] runs when the user types after a commit, so the caller can clear a refusal shown below.
 * [label] names the field for screen readers.
 */
@Composable
fun NotekeaTitleField(
    value: String,
    onCommit: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onEdit: () -> Unit = {}
) {
    val state = remember(value) { TextFieldState(initialText = value) }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(value = false) }
    val currentOnEdit by rememberUpdatedState(newValue = onEdit)
    LaunchedEffect(state) { snapshotFlow { state.text.toString() }.drop(count = 1).collect { currentOnEdit() } }
    val commit = {
        val text = state.text.toString()
        if (text != value) onCommit(text)
    }
    BasicTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label }
            .onFocusChanged {
                if (isFocused && !it.isFocused) commit()
                isFocused = it.isFocused
            }.onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    in SUBMIT_KEYS -> focusManager.clearFocus()
                    Key.Escape -> state.setTextAndPlaceCursorAtEnd(text = value).also { focusManager.clearFocus() }
                    else -> return@onPreviewKeyEvent false
                }
                true
            }.titleUnderline(color = underlineColor(isFocused = isFocused, isError = isError)),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        ),
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun underlineColor(
    isFocused: Boolean,
    isError: Boolean
): Color = when {
    isError -> MaterialTheme.colorScheme.error
    isFocused -> MaterialTheme.colorScheme.primary
    else -> Color.Transparent
}

private fun Modifier.titleUnderline(
    color: Color
): Modifier = drawBehind {
    val width = NotekeaDimensions.BorderWidth.toPx()
    val y = size.height - width / 2
    drawLine(color = color, start = Offset(x = 0f, y = y), end = Offset(x = size.width, y = y), strokeWidth = width)
}

@Preview
@Composable
private fun NotekeaTitleFieldPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaTitleField(value = PREVIEW_TITLE, onCommit = {}, label = PREVIEW_TITLE)
            Box(modifier = Modifier.height(height = NotekeaDimensions.SpacingM))
            NotekeaTitleField(value = PREVIEW_TITLE, onCommit = {}, label = PREVIEW_TITLE, isError = true)
        }
    }
}

package app.notekea.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val FIELD_VERTICAL_PADDING_DP = 2
private const val PREVIEW_PLACEHOLDER = "Empty"
private const val PREVIEW_VALUE = "In progress"
private const val PREVIEW_WIDTH_DP = 240
private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

/**
 * A value edited in place, as in a form of properties: plain text until hovered (a faint border) or focused (the
 * primary border). What is typed is kept here and handed to [onCommit] when Enter is pressed or the field is left;
 * Esc puts [value] back. Text [isValid] rejects is outlined in the error color and not committed. [placeholder] shows
 * while it is empty. [displayValue], when given, is shown instead of [value] while the field is not focused (a date
 * written for people), and [value] comes back to be edited. [textColor] replaces the ink color (a property's
 * muted name).
 */
@Composable
fun NotekeaPropertyField(
    value: String,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    displayValue: String? = null,
    textColor: Color = Color.Unspecified,
    isValid: (String) -> Boolean = { true }
) {
    var text by remember(value) { mutableStateOf(value = value) }
    val interactions = remember { MutableInteractionSource() }
    val isFocused by interactions.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val isError = !isValid(text)
    val commit = { if (text != value && isValid(text)) onCommit(text) }
    val isShowingDisplay = !isFocused && displayValue != null && text == value
    BasicTextField(
        value = if (isShowingDisplay) displayValue else text,
        onValueChange = { if (!isShowingDisplay) text = it },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (!it.isFocused) commit() }
            .onPreviewKeyEvent { event ->
                keyAction(event = event, focusManager = focusManager, onCancel = { text = value })
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = textColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
        ),
        singleLine = true,
        interactionSource = interactions,
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        decorationBox = { field ->
            FieldFrame(isFocused = isFocused, isError = isError, interactions = interactions) {
                if (text.isEmpty() && placeholder != null) PlaceholderText(text = placeholder)
                field()
            }
        }
    )
}

/** Enter leaves the field (which commits); Esc puts the value back and leaves it. Returns whether it was one. */
private fun keyAction(
    event: KeyEvent,
    focusManager: FocusManager,
    onCancel: () -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when (event.key) {
        in SUBMIT_KEYS -> {
            focusManager.clearFocus()
            true
        }

        Key.Escape -> {
            onCancel()
            focusManager.clearFocus()
            true
        }

        else -> false
    }
}

@Composable
private fun FieldFrame(
    isFocused: Boolean,
    isError: Boolean,
    interactions: MutableInteractionSource,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isHovered by interactions.collectIsHoveredAsState()
    val border = when {
        isError -> colors.error
        isFocused -> colors.primary
        isHovered -> colors.outline
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .border(width = NotekeaDimensions.BorderWidth, color = border, shape = MaterialTheme.shapes.extraSmall)
            .padding(horizontal = NotekeaDimensions.SpacingXs, vertical = FIELD_VERTICAL_PADDING_DP.dp)
    ) { content() }
}

@Composable
private fun PlaceholderText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun NotekeaPropertyFieldPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaPropertyField(value = PREVIEW_VALUE, onCommit = {}, placeholder = PREVIEW_PLACEHOLDER)
        }
    }
}

@Preview
@Composable
private fun NotekeaPropertyFieldEmptyPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaPropertyField(value = "", onCommit = {}, placeholder = PREVIEW_PLACEHOLDER)
        }
    }
}

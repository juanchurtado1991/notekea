package app.notekea.core.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_CANCEL = "Cancel"
private const val PREVIEW_DELETE = "Delete note"
private const val PREVIEW_DISABLED = "Disabled"
private const val PREVIEW_OPEN = "Open folder…"
private const val PREVIEW_REMOVE = "Remove"
private const val PREVIEW_SHORTCUT = "Ctrl+O"
private const val SHORTCUT_HINT_ALPHA = 0.72f

/**
 * Text button in the four Notekea weights with the 6 dp control radius; colors come from semantic roles.
 * [shortcutHint] shows the keyboard shortcut ("Ctrl+O", "⌘O") as a muted label after the text, so power users
 * discover it without it being the only way in (`GUIDELINES.md`, product principle). A focused button shows its focus
 * as a border.
 */
@Composable
fun NotekeaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NotekeaButtonStyle = NotekeaButtonStyle.PRIMARY,
    enabled: Boolean = true,
    shortcutHint: String? = null
) {
    val label: @Composable RowScope.() -> Unit = {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
        shortcutHint?.let { ShortcutHint(text = it) }
    }
    // Read from the focus modifier, not from interactions: a dialog may focus the button before it collects them.
    var isFocused by remember { mutableStateOf(value = false) }
    StyledButton(
        style = style,
        onClick = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        enabled = enabled,
        border = borderOf(style = style, enabled = enabled, isFocused = isFocused),
        label = label
    )
}

@Composable
private fun StyledButton(
    style: NotekeaButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    border: BorderStroke?,
    label: @Composable RowScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.small
    when (style) {
        NotekeaButtonStyle.DESTRUCTIVE -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                border = border
            ) { label() }
        }

        NotekeaButtonStyle.PRIMARY -> {
            Button(onClick = onClick, modifier = modifier, enabled = enabled, shape = shape, border = border) {
                label()
            }
        }

        NotekeaButtonStyle.SECONDARY -> {
            OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled, shape = shape, border = border) {
                label()
            }
        }

        NotekeaButtonStyle.TEXT -> {
            TextButton(onClick = onClick, modifier = modifier, enabled = enabled, shape = shape, border = border) {
                label()
            }
        }
    }
}

/**
 * Keyboard focus is drawn as the button's own border (2 dp, on its shape, no gap): primary, or the text color on
 * filled buttons, where primary would not show. Unfocused, only the outlined button keeps its 1 dp outline.
 */
@Composable
private fun borderOf(
    style: NotekeaButtonStyle,
    enabled: Boolean,
    isFocused: Boolean
): BorderStroke? {
    val isFilled = style == NotekeaButtonStyle.PRIMARY || style == NotekeaButtonStyle.DESTRUCTIVE
    val focusColor = if (isFilled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    return when {
        isFocused -> BorderStroke(width = NotekeaDimensions.FocusRingWidth, color = focusColor)
        style == NotekeaButtonStyle.SECONDARY -> ButtonDefaults.outlinedButtonBorder(enabled = enabled)
        else -> null
    }
}

@Composable
private fun ShortcutHint(
    text: String
) {
    Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = LocalContentColor.current.copy(alpha = SHORTCUT_HINT_ALPHA)
    )
}

@Preview
@Composable
private fun NotekeaButtonPreview() {
    NotekeaPreviewThemes {
        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaButton(text = PREVIEW_OPEN, onClick = {}, shortcutHint = PREVIEW_SHORTCUT)
            NotekeaButton(text = PREVIEW_CANCEL, onClick = {}, style = NotekeaButtonStyle.SECONDARY)
            NotekeaButton(text = PREVIEW_DELETE, onClick = {}, style = NotekeaButtonStyle.DESTRUCTIVE)
            NotekeaButton(text = PREVIEW_REMOVE, onClick = {}, style = NotekeaButtonStyle.TEXT)
            NotekeaButton(text = PREVIEW_DISABLED, onClick = {}, enabled = false)
        }
    }
}

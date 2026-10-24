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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
private const val FOCUS_RING_GAP_DP = 2
private const val SHORTCUT_HINT_ALPHA = 0.72f

/**
 * Text button in the four Notekea weights with the 6 dp control radius; colors come from semantic roles.
 * [shortcutHint] shows the keyboard shortcut ("Ctrl+O", "⌘O") as a muted label after the text, so power users
 * discover it without it being the only way in (`GUIDELINES.md`, product principle). A focused button shows a focus
 * ring around it.
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
    val modifier = modifier.then(other = rememberFocusRing())
    when (style) {
        NotekeaButtonStyle.DESTRUCTIVE -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) { label() }

        NotekeaButtonStyle.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small
        ) { label() }

        NotekeaButtonStyle.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small
        ) { label() }

        NotekeaButtonStyle.TEXT -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.small
        ) { label() }
    }
}

@Composable
private fun rememberFocusRing(): Modifier {
    var isFocused by remember { mutableStateOf(value = false) }
    val color = MaterialTheme.colorScheme.primary
    return Modifier.onFocusChanged { isFocused = it.isFocused }.focusRing(isFocused = isFocused, color = color)
}

/**
 * A ring drawn just outside the button while it has focus, so the dialog's initial focus and Tab moves can be seen
 * (Material's own focus hint is a faint overlay). It does not change the layout.
 */
private fun Modifier.focusRing(
    isFocused: Boolean,
    color: Color
): Modifier = drawWithContent {
    drawContent()
    if (isFocused) {
        val width = NotekeaDimensions.FocusRingWidth.toPx()
        val outset = FOCUS_RING_GAP_DP.dp.toPx() + width / 2
        drawRoundRect(
            color = color,
            topLeft = Offset(x = -outset, y = -outset),
            size = Size(width = size.width + outset * 2, height = size.height + outset * 2),
            cornerRadius = CornerRadius(x = NotekeaDimensions.ControlRadius.toPx() + outset),
            style = Stroke(width = width)
        )
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

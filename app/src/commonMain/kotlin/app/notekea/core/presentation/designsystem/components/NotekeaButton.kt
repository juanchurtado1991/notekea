package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_CANCEL = "Cancel"
private const val PREVIEW_DELETE = "Delete note"
private const val PREVIEW_DISABLED = "Disabled"
private const val PREVIEW_OPEN = "Open folder…"
private const val PREVIEW_REMOVE = "Remove"
private const val PREVIEW_SHORTCUT = "Ctrl+O"
private const val SHORTCUT_HINT_ALPHA = 0.72f
private val BUTTON_PADDING = PaddingValues(
    horizontal = NotekeaDimensions.SpacingM,
    vertical = NotekeaDimensions.SpacingXs
)

/**
 * Text button in the four Notekea weights, as tall as the app's other controls ([NotekeaDimensions.ControlHeight]);
 * colors come from semantic roles. [shortcutHint] shows the keyboard shortcut ("Ctrl+O", "⌘O") as a muted label after
 * the text, so it is discovered without being the only way in. A focused button shows its focus as a border.
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
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )

        shortcutHint?.let { ShortcutHint(text = it) }
    }

    // Read from the focus modifier, not from interactions: a dialog may focus the button before it collects them.
    var isFocused by remember { mutableStateOf(value = false) }

    // Material's 48 dp touch target is for phones: a pointer needs none around the 32 dp button.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        StyledButton(
            style = style,
            onClick = onClick,
            modifier = modifier
                .heightIn(min = NotekeaDimensions.ControlHeight)
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            border = borderOf(
                style = style,
                enabled = enabled,
                isFocused = isFocused
            ),
            label = label
        )
    }
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
        NotekeaButtonStyle.DESTRUCTIVE, NotekeaButtonStyle.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = filledColors(style = style),
            border = border,
            contentPadding = BUTTON_PADDING
        ) { label() }

        NotekeaButtonStyle.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            border = border,
            contentPadding = BUTTON_PADDING
        ) { label() }

        NotekeaButtonStyle.TEXT -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            border = border,
            contentPadding = BUTTON_PADDING
        ) { label() }
    }
}

/** The error colors for a destructive button, the primary ones otherwise. */
@Composable
private fun filledColors(style: NotekeaButtonStyle): ButtonColors =
    if (style == NotekeaButtonStyle.DESTRUCTIVE) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    } else {
        ButtonDefaults.buttonColors()
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

    val focusColor =
        if (isFilled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary

    return when {
        isFocused -> BorderStroke(
            width = NotekeaDimensions.FocusRingWidth,
            color = focusColor
        )

        style == NotekeaButtonStyle.SECONDARY -> ButtonDefaults.outlinedButtonBorder(
            enabled = enabled
        )

        else -> null
    }
}

@Composable
private fun ShortcutHint(text: String) {
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
            NotekeaButton(
                text = PREVIEW_OPEN,
                onClick = {},
                shortcutHint = PREVIEW_SHORTCUT
            )

            NotekeaButton(
                text = PREVIEW_CANCEL,
                onClick = {},
                style = NotekeaButtonStyle.SECONDARY
            )

            NotekeaButton(
                text = PREVIEW_DELETE,
                onClick = {},
                style = NotekeaButtonStyle.DESTRUCTIVE
            )

            NotekeaButton(
                text = PREVIEW_REMOVE,
                onClick = {},
                style = NotekeaButtonStyle.TEXT
            )

            NotekeaButton(
                text = PREVIEW_DISABLED,
                onClick = {},
                enabled = false
            )
        }
    }
}

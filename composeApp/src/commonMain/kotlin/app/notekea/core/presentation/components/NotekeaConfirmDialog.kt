package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_CANCEL = "Cancel"
private const val PREVIEW_DELETE = "Delete note"
private const val PREVIEW_MESSAGE = "“Tomato plan” will be moved to the trash. You can restore it from there."
private const val PREVIEW_TITLE = "Delete this note?"

/**
 * A question that needs an answer before going on (deleting a note). The buttons follow the platform's order
 * ([buttonOrder]: Cancel first on macOS and Linux, last on Windows, `PLATFORM_GUIDELINES.md` §5) and the confirm
 * button says what it does ("Delete note", never "OK"). A destructive confirm button uses the error color and focus
 * starts on Cancel, so a quick Enter never destroys anything; otherwise focus starts on the confirm button. Esc and
 * clicking outside cancel.
 */
@Composable
fun NotekeaConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    buttonOrder: DialogButtonOrder,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogButtons(
                confirmLabel = confirmLabel,
                cancelLabel = cancelLabel,
                buttonOrder = buttonOrder,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                isDestructive = isDestructive
            )
        },
        modifier = modifier.border(
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = colors.outline),
            shape = MaterialTheme.shapes.medium
        ),
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        shape = MaterialTheme.shapes.medium,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant
    )
}

@Composable
private fun DialogButtons(
    confirmLabel: String,
    cancelLabel: String,
    buttonOrder: DialogButtonOrder,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean
) {
    val initialFocus = remember { FocusRequester() }
    LaunchedEffect(key1 = initialFocus) { initialFocus.requestFocus() }
    val confirm: @Composable () -> Unit = {
        NotekeaButton(
            text = confirmLabel,
            onClick = onConfirm,
            modifier = if (isDestructive) Modifier else Modifier.focusRequester(focusRequester = initialFocus),
            style = if (isDestructive) NotekeaButtonStyle.DESTRUCTIVE else NotekeaButtonStyle.PRIMARY
        )
    }
    val cancel: @Composable () -> Unit = {
        NotekeaButton(
            text = cancelLabel,
            onClick = onDismiss,
            modifier = if (isDestructive) Modifier.focusRequester(focusRequester = initialFocus) else Modifier,
            style = NotekeaButtonStyle.SECONDARY
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        when (buttonOrder) {
            DialogButtonOrder.CANCEL_THEN_PRIMARY -> {
                cancel()
                confirm()
            }

            DialogButtonOrder.PRIMARY_THEN_CANCEL -> {
                confirm()
                cancel()
            }
        }
    }
}

@Preview
@Composable
private fun NotekeaConfirmDialogPreview() {
    NotekeaPreviewThemes {
        NotekeaConfirmDialog(
            title = PREVIEW_TITLE,
            message = PREVIEW_MESSAGE,
            confirmLabel = PREVIEW_DELETE,
            cancelLabel = PREVIEW_CANCEL,
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            onConfirm = {},
            onDismiss = {},
            isDestructive = true
        )
    }
}

@Preview
@Composable
private fun NotekeaConfirmDialogWindowsOrderPreview() {
    NotekeaPreviewThemes {
        NotekeaConfirmDialog(
            title = PREVIEW_TITLE,
            message = PREVIEW_MESSAGE,
            confirmLabel = PREVIEW_DELETE,
            cancelLabel = PREVIEW_CANCEL,
            buttonOrder = DialogButtonOrder.PRIMARY_THEN_CANCEL,
            onConfirm = {},
            onDismiss = {},
            isDestructive = true
        )
    }
}

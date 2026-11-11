package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val ALERT_ICON_DP = 48
private const val ALERT_WIDTH_DP = 232
private const val PREVIEW_CANCEL = "Cancel"
private const val PREVIEW_DELETE = "Delete note"
private const val PREVIEW_MESSAGE =
    "“Tomato plan” will be moved to the trash. You can restore it from there."
private const val PREVIEW_TITLE = "Delete this note?"

/**
 * A question that needs an answer before going on (deleting a note). The buttons follow the platform's order
 * ([buttonOrder], `PLATFORM_GUIDELINES.md` §5). A destructive confirm uses the error color and focus starts on Cancel,
 * so a quick Enter never destroys anything. Esc and clicking outside cancel.
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
    isDestructive: Boolean = false,
    isStacked: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val align = if (isStacked) TextAlign.Center else TextAlign.Start

    // macOS's alert (8A): the app's icon over a centered title, the buttons stacked across its width.
    val alertIcon: (@Composable () -> Unit)? = if (isStacked) {
        { NotekeaLogo(size = ALERT_ICON_DP.dp) }
    } else {
        null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogButtons(
                confirmLabel = confirmLabel,
                cancelLabel = cancelLabel,
                buttonOrder = buttonOrder,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                isDestructive = isDestructive,
                isStacked = isStacked
            )
        },
        icon = alertIcon,
        modifier = modifier.border(
            border = BorderStroke(
                width = NotekeaDimensions.BorderWidth,
                color = colors.outline
            ),
            shape = MaterialTheme.shapes.medium
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = align
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = align
            )
        },
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
    isDestructive: Boolean,
    isStacked: Boolean
) {
    val initialFocus = remember { FocusRequester() }
    val width = if (isStacked) Modifier.fillMaxWidth() else Modifier

    LaunchedEffect(key1 = initialFocus) { initialFocus.requestFocus() }

    val confirm: @Composable () -> Unit = {
        val confirmModifier = if (isDestructive) {
            Modifier
        } else {
            Modifier.focusRequester(
                focusRequester = initialFocus
            )
        }

        val style =
            if (isDestructive) NotekeaButtonStyle.DESTRUCTIVE else NotekeaButtonStyle.PRIMARY

        NotekeaButton(
            text = confirmLabel,
            onClick = onConfirm,
            modifier = confirmModifier.then(other = width),
            style = style
        )
    }

    val cancel: @Composable () -> Unit = {
        val cancelModifier = if (isDestructive) {
            Modifier.focusRequester(
                focusRequester = initialFocus
            )
        } else {
            Modifier
        }

        NotekeaButton(
            text = cancelLabel,
            onClick = onDismiss,
            modifier = cancelModifier.then(other = width),
            style = NotekeaButtonStyle.SECONDARY
        )
    }

    if (isStacked) {
        Column(
            modifier = Modifier.width(width = ALERT_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            confirm()
            cancel()
        }

        return
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
private fun NotekeaConfirmDialogMacPreview() {
    NotekeaPreviewThemes {
        NotekeaConfirmDialog(
            title = PREVIEW_TITLE,
            message = PREVIEW_MESSAGE,
            confirmLabel = PREVIEW_DELETE,
            cancelLabel = PREVIEW_CANCEL,
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
            isStacked = true
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

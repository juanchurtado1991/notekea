package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_CANCEL = "Cancel"
private const val PREVIEW_CLIP = "Clip"

/**
 * The buttons that end a dialog, at its end and in the platform's order (`PLATFORM_GUIDELINES.md` §5): [buttons] are
 * written from the one that cancels to the primary one, and with [DialogButtonOrder.PRIMARY_THEN_CANCEL] (Windows)
 * they show the other way round. The one that cancels is a secondary button, the choices between a text or secondary.
 */
@Composable
fun NotekeaDialogButtons(
    buttonOrder: DialogButtonOrder,
    buttons: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier
) {
    val shown = when (buttonOrder) {
        DialogButtonOrder.CANCEL_THEN_PRIMARY -> buttons
        DialogButtonOrder.PRIMARY_THEN_CANCEL -> buttons.asReversed()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.End
        )
    ) {
        shown.forEach { it() }
    }
}

@Preview
@Composable
private fun NotekeaDialogButtonsPreview() {
    NotekeaPreviewThemes {
        NotekeaDialogButtons(
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            buttons = listOf(
                {
                    NotekeaButton(
                        text = PREVIEW_CANCEL,
                        onClick = {},
                        style = NotekeaButtonStyle.SECONDARY
                    )
                },
                {
                    NotekeaButton(
                        text = PREVIEW_CLIP,
                        onClick = {}
                    )
                }
            )
        )
    }
}

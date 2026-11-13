package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.CoversWindow

private const val ELEVATION_DP = 12
private const val DEFAULT_SIDE_DP = 340
private const val MIN_SIDE_DP = 260
private const val PREVIEW_HEIGHT_DP = 200
private const val PREVIEW_TEXT = "About Notekea"
private const val PREVIEW_WIDTH_DP = 360

/**
 * A modal panel over the window (About, Settings): the window dims behind it, and Esc or a click outside closes it
 * ([onDismiss]). [content] lays itself out at the size [modifier] gives it; a [side] card as tall, [sideWidth] or what
 * the window leaves (not under [MIN_SIDE_DP], else left out), beside it; both centered together.
 */
@Composable
fun NotekeaDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    side: (@Composable () -> Unit)? = null,
    sideWidth: Dp = DEFAULT_SIDE_DP.dp,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CoversWindow()

        Layout(
            contents = listOf(
                {
                    NotekeaDialogSurface(
                        modifier = modifier,
                        content = content
                    )
                },
                {
                    side?.let {
                        NotekeaDialogSurface(
                            modifier = Modifier,
                            content = it
                        )
                    }
                }
            )
        ) { (mains, sides), constraints ->
            val main = mains.first().measure(constraints = constraints)
            val gap = NotekeaDimensions.SpacingM.roundToPx()
            val room = minOf(sideWidth.roundToPx(), constraints.maxWidth - main.width - gap)

            val beside = sides
                .firstOrNull()
                ?.takeIf {
                    room >= MIN_SIDE_DP.dp.roundToPx()
                }
                ?.measure(
                    constraints = Constraints.fixed(
                        width = room,
                        height = main.height
                    )
                )

            val width = main.width + (
                beside?.let {
                    it.width + gap
                } ?: 0
                )

            layout(
                width = width,
                height = main.height
            ) {
                main.place(
                    x = 0,
                    y = 0
                )

                beside?.place(
                    x = main.width + gap,
                    y = 0
                )
            }
        }
    }
}

/** The dialog's card: the large shape, the surface color, a hairline and a shadow. */
@Composable
private fun NotekeaDialogSurface(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = NotekeaDimensions.BorderWidth,
            color = MaterialTheme.colorScheme.outline
        ),
        shadowElevation = ELEVATION_DP.dp,
        content = content
    )
}

@Preview
@Composable
private fun NotekeaDialogPreview() {
    NotekeaPreviewThemes {
        NotekeaDialogSurface(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl)) {
                Text(text = PREVIEW_TEXT)
            }
        }
    }
}

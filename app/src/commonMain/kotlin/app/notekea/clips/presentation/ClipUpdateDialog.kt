package app.notekea.clips.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.clips.domain.UpdateStep
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaProgressBar
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.clip_cancel
import app.notekea.resources.clip_update_close
import app.notekea.resources.clip_update_done
import app.notekea.resources.clip_update_failed
import app.notekea.resources.clip_update_none
import app.notekea.resources.clip_update_reading
import app.notekea.resources.clip_update_starting
import app.notekea.resources.clip_update_title
import org.jetbrains.compose.resources.stringResource

private const val DIALOG_WIDTH_DP = 520

/**
 * "Update web sources" (the developer's request of 2026-11-13): how far the update is, page by page with a bar, then
 * what it did, how many notes it updated, added and left as they were; Cancel stops it, what is written stays.
 */
@Composable
fun ClipUpdateDialog(
    state: ClipState,
    onIntent: (ClipIntent) -> Unit
) {
    if (!state.isUpdateShown) return
    val dismiss = { onIntent(ClipIntent.OnUpdateDismiss) }
    val done = state.update as? UpdateStep.Done

    NotekeaDialog(
        onDismiss = dismiss,
        modifier = Modifier.width(width = DIALOG_WIDTH_DP.dp)
    ) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = stringResource(resource = Res.string.clip_update_title),
                style = MaterialTheme.typography.titleMedium
            )

            val reading = state.update as? UpdateStep.Reading
            if (done != null) Summary(done = done) else Progress(reading = reading)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val close =
                    if (done != null) Res.string.clip_update_close else Res.string.clip_cancel

                val style =
                    if (done != null) NotekeaButtonStyle.PRIMARY else NotekeaButtonStyle.TEXT

                NotekeaButton(
                    text = stringResource(resource = close),
                    onClick = dismiss,
                    style = style
                )
            }
        }
    }
}

@Composable
private fun Progress(reading: UpdateStep.Reading?) {
    val text = if (reading == null) {
        stringResource(resource = Res.string.clip_update_starting)
    } else {
        stringResource(
            Res.string.clip_update_reading,
            reading.done + 1,
            reading.total,
            reading.url.substringAfter(delimiter = "://")
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )

    val fraction = reading?.let {
        it.done.toFloat() / it.total.coerceAtLeast(minimumValue = 1)
    }

    NotekeaProgressBar(
        label = stringResource(resource = Res.string.clip_update_title),
        fraction = fraction,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Summary(done: UpdateStep.Done) {
    val total = done.updated.size + done.added.size + done.unchanged + done.failed

    val text = if (total == 0) {
        stringResource(resource = Res.string.clip_update_none)
    } else {
        stringResource(
            Res.string.clip_update_done,
            done.updated.size,
            done.added.size,
            done.unchanged
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )

    if (done.failed > 0) {
        Text(
            text = stringResource(Res.string.clip_update_failed, done.failed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun ClipUpdateDialogPreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)) {
            ClipUpdateDialog(
                state = ClipState(
                    isUpdateShown = true,
                    update = UpdateStep.Reading(
                        done = 11,
                        total = 45,
                        url = "https://developer.android.com/develop/ui/compose/layouts"
                    )
                ),
                onIntent = {}
            )

            ClipUpdateDialog(
                state = ClipState(
                    isUpdateShown = true,
                    update = UpdateStep.Done(
                        updated = listOf(RelativePath(value = "Web sources/Docs.md")),
                        unchanged = 27,
                        failed = 1
                    )
                ),
                onIntent = {}
            )
        }
    }
}

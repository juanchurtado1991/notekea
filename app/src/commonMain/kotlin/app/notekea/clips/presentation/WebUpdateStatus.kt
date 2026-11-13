package app.notekea.clips.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.clips.domain.UpdateStep
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.components.layout.NotekeaStatusLabel
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.clip_update_status
import app.notekea.resources.clip_update_status_tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The update of the web sources in the status bar (the developer's request of 2026-11-13): "Web sources · 12 of 45"
 * with a ring that fills while it runs, asked for or on its own; nothing otherwise. A click shows its dialog.
 */
@Composable
fun WebUpdateStatus(
    state: ClipState,
    onIntent: (ClipIntent) -> Unit
) {
    val reading = state.update as? UpdateStep.Reading ?: return
    val total = reading.total.coerceAtLeast(minimumValue = 1)

    NotekeaStatusLabel(
        text = stringResource(Res.string.clip_update_status, reading.done + 1, total),
        tooltip = stringResource(resource = Res.string.clip_update_status_tooltip),
        onClick = { onIntent(ClipIntent.OnUpdateShow) },
        leading = { NotekeaStatusRing(fraction = reading.done.toFloat() / total) }
    )
}

@Preview
@Composable
private fun WebUpdateStatusPreview() {
    NotekeaPreviewThemes {
        WebUpdateStatus(
            state = ClipState(
                update = UpdateStep.Reading(
                    done = 11,
                    total = 45,
                    url = "https://developer.android.com/develop/ui"
                )
            ),
            onIntent = {}
        )
    }
}

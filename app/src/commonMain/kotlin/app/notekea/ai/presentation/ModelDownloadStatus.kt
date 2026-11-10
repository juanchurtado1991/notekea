package app.notekea.ai.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelError
import app.notekea.ai.domain.ModelSize
import app.notekea.core.presentation.designsystem.components.NotekeaStatusLabel
import app.notekea.core.presentation.designsystem.components.NotekeaStatusRing
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.models_status_downloading
import app.notekea.resources.models_status_failed
import app.notekea.resources.models_status_paused
import app.notekea.resources.models_status_tooltip
import app.notekea.resources.models_status_verifying
import org.jetbrains.compose.resources.stringResource

private const val PERCENT = 100
private const val PREVIEW_DONE = 420L
private const val PREVIEW_TOTAL = 1_000L

/**
 * The chat model's installation in the status bar: "AI model · 42 %" with a ring while it downloads, paused, checked
 * or failed; nothing when no model installs. A click opens Settings → AI model ([ModelDownloadIntent.OnOpen]).
 */
@Composable
fun ModelDownloadStatus(
    state: ModelDownloadState,
    onIntent: (ModelDownloadIntent) -> Unit
) {
    val session = state.session ?: return
    val fraction = state.fraction
    val percent = ((fraction ?: 0f) * PERCENT).toInt()
    val onClick = { onIntent(ModelDownloadIntent.OnOpen) }
    val tooltip = stringResource(resource = Res.string.models_status_tooltip)

    val text = when {
        session.progress is InstallProgress.Failed ->
            stringResource(resource = Res.string.models_status_failed)

        session.progress == InstallProgress.Verifying ->
            stringResource(resource = Res.string.models_status_verifying)

        session.isPaused -> stringResource(Res.string.models_status_paused, percent)

        fraction != null -> stringResource(Res.string.models_status_downloading, percent)

        else -> return
    }

    val isRunning = fraction != null && !session.isPaused

    NotekeaStatusLabel(
        text = text,
        icon = NotekeaIcons.FeatureAi.takeUnless { isRunning },
        tooltip = tooltip,
        onClick = onClick,
        leading = if (isRunning) ({ NotekeaStatusRing(fraction = fraction) }) else null
    )
}

@Preview
@Composable
private fun ModelDownloadStatusPreview() {
    val downloading = InstallSession(
        size = ModelSize.MEDIUM,
        progress = InstallProgress.Downloading(
            doneBytes = PREVIEW_DONE,
            totalBytes = PREVIEW_TOTAL
        )
    )

    NotekeaPreviewThemes {
        Column {
            listOf(
                downloading,
                downloading.copy(isPaused = true),
                downloading.copy(progress = InstallProgress.Verifying),
                downloading.copy(progress = InstallProgress.Failed(error = ModelError.Network))
            ).forEach {
                ModelDownloadStatus(
                    state = ModelDownloadState(session = it),
                    onIntent = {}
                )
            }
        }
    }
}

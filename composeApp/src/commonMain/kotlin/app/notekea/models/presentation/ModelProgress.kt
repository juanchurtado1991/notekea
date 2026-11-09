package app.notekea.models.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaProgressBar
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.ModelSize
import app.notekea.resources.Res
import app.notekea.resources.models_cancel
import app.notekea.resources.models_pause
import app.notekea.resources.models_paused
import app.notekea.resources.models_progress
import app.notekea.resources.models_progress_label
import app.notekea.resources.models_progress_speed
import app.notekea.resources.models_resume
import app.notekea.resources.models_verifying
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_DONE = 1_400_000_000L
private const val PREVIEW_TOTAL = 3_180_000_000L
private const val PREVIEW_WIDTH_DP = 420

/**
 * An installation under way: the bar, the gigabytes done of the total (or "Checking the file…" while its SHA-256 is
 * checked, when it cannot be paused), and Pause or Resume next to Cancel. Until the files to download are known (a
 * total of 0) the bar only moves.
 */
@Composable
internal fun ModelProgress(
    size: ModelSize,
    status: ModelStatus.Installing,
    onIntent: (ModelManagerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val downloading = (status.progress as? InstallProgress.Downloading)?.takeIf { it.totalBytes > 0 }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        NotekeaProgressBar(
            fraction = downloading?.let { it.doneBytes.toFloat() / it.totalBytes.coerceAtLeast(minimumValue = 1) },
            label = stringResource(resource = Res.string.models_progress_label),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            Text(
                text = progressText(progress = status.progress, isPaused = status.isPaused).orEmpty(),
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (downloading != null) ProgressButtons(size = size, isPaused = status.isPaused, onIntent = onIntent)
        }
    }
}

/** Pause or Resume, and Cancel, while the files download. */
@Composable
private fun ProgressButtons(
    size: ModelSize,
    isPaused: Boolean,
    onIntent: (ModelManagerIntent) -> Unit
) {
    NotekeaButton(
        text = stringResource(resource = if (isPaused) Res.string.models_resume else Res.string.models_pause),
        onClick = { onIntent(if (isPaused) ModelManagerIntent.OnInstall(size = size) else ModelManagerIntent.OnPause) },
        style = NotekeaButtonStyle.SECONDARY
    )
    NotekeaButton(
        text = stringResource(resource = Res.string.models_cancel),
        onClick = { onIntent(ModelManagerIntent.OnCancel) },
        style = NotekeaButtonStyle.TEXT
    )
}

@Composable
private fun progressText(
    progress: InstallProgress,
    isPaused: Boolean
): String? {
    val downloading = progress as? InstallProgress.Downloading
        ?: return stringResource(resource = Res.string.models_verifying)
    if (downloading.totalBytes == 0L) return null
    val done = formatBytes(bytes = downloading.doneBytes)
    val total = formatBytes(bytes = downloading.totalBytes)
    val speed = downloading.bytesPerSecond
    return when {
        isPaused -> stringResource(resource = Res.string.models_paused, done, total)

        speed == null -> stringResource(resource = Res.string.models_progress, done, total)

        else -> stringResource(
            resource = Res.string.models_progress_speed,
            done,
            total,
            formatSpeed(bytesPerSecond = speed)
        )
    }
}

@Preview
@Composable
private fun ModelProgressPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            listOf(false, true).forEach { isPaused ->
                ModelProgress(
                    size = ModelSize.MEDIUM,
                    status = ModelStatus.Installing(
                        progress = InstallProgress.Downloading(doneBytes = PREVIEW_DONE, totalBytes = PREVIEW_TOTAL),
                        isPaused = isPaused
                    ),
                    onIntent = {}
                )
            }
            ModelProgress(
                size = ModelSize.MEDIUM,
                status = ModelStatus.Installing(progress = InstallProgress.Verifying, isPaused = false),
                onIntent = {}
            )
        }
    }
}

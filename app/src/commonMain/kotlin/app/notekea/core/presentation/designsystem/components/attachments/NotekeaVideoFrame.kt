package app.notekea.core.presentation.designsystem.components.attachments

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val VIDEO_RATIO = 16f / 9f
private const val TRACK_DP = 4
private const val TRACK_TOUCH_DP = 16
private const val PREVIEW_WIDTH_DP = 420
private const val PREVIEW_PROGRESS = 0.22f
private const val PREVIEW_TIME = "0:42 / 3:10"

/**
 * A video playing in a note: the [video] at 16:9, and under it play or pause, how far it is ([progress], a bar a click
 * moves, and its [time], "0:42 / 3:10") and ✕, which closes it back to its card ([onClose]). The two that change as it
 * plays are read where they are drawn.
 */
@Composable
fun NotekeaVideoFrame(
    isPlaying: Boolean,
    progress: () -> Float,
    time: () -> String,
    labels: VideoFrameLabels,
    onPlayToggle: () -> Unit,
    onSeek: (fraction: Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    video: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.small)
            .background(color = colors.surfaceContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = VIDEO_RATIO)
                .background(color = colors.scrim)
        ) { video() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = NotekeaDimensions.SpacingXs),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotekeaIconButton(
                icon = if (isPlaying) NotekeaIcons.Pause else NotekeaIcons.Play,
                label = if (isPlaying) labels.pause else labels.play,
                onClick = onPlayToggle
            )

            SeekBar(
                progress = progress,
                label = labels.seek,
                onSeek = onSeek,
                modifier = Modifier.weight(weight = 1f)
            )

            Time(time = time)

            NotekeaIconButton(
                icon = NotekeaIcons.Close,
                label = labels.close,
                onClick = onClose
            )
        }
    }
}

// How far in the video is, as a bar; a click on it moves there.
@Composable
private fun SeekBar(
    progress: () -> Float,
    label: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = modifier
            .height(height = TRACK_TOUCH_DP.dp)
            .semantics { contentDescription = label }
            .pointerInput(onSeek) {
                detectTapGestures { offset -> onSeek((offset.x / size.width).coerceIn(0f, 1f)) }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = TRACK_DP.dp)
                .clip(shape = MaterialTheme.shapes.extraSmall)
                .background(color = colors.outlineVariant)
        )

        Box(
            modifier = Modifier
                .width(width = maxWidth * progress().coerceIn(0f, 1f))
                .height(height = TRACK_DP.dp)
                .clip(shape = MaterialTheme.shapes.extraSmall)
                .background(color = colors.primary)
        )
    }
}

@Composable
private fun Time(time: () -> String) {
    Text(
        text = time(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun NotekeaVideoFramePreview() {
    NotekeaPreviewThemes {
        NotekeaVideoFrame(
            isPlaying = true,
            progress = { PREVIEW_PROGRESS },
            time = { PREVIEW_TIME },
            labels = VideoFrameLabels(
                play = "Play",
                pause = "Pause",
                seek = "Position",
                close = "Close"
            ),
            onPlayToggle = {},
            onSeek = {},
            onClose = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        ) { Box(modifier = Modifier.fillMaxHeight()) }
    }
}

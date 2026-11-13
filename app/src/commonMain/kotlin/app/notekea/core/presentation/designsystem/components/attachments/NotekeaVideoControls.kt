package app.notekea.core.presentation.designsystem.components.attachments

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
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

private const val TRACK_DP = 4
private const val TRACK_TOUCH_DP = 16
private const val VOLUME_WIDTH_DP = 72
private const val PREVIEW_WIDTH_DP = 520
private const val PREVIEW_PROGRESS = 0.22f
private const val PREVIEW_VOLUME = 0.7f
private const val PREVIEW_TIME = "0:42 / 3:10"

/**
 * A video's controls, one row: play or pause, how far it is ([progress], a bar a click moves, and its [time], "0:42 /
 * 3:10"), mute with the [volume] (0 to 1, a bar a click or a drag moves) and full screen. What changes as the video
 * plays is read where it is drawn.
 */
@Composable
fun NotekeaVideoControls(
    isPlaying: Boolean,
    isFullscreen: Boolean,
    progress: () -> Float,
    time: () -> String,
    volume: () -> Float,
    labels: VideoFrameLabels,
    actions: VideoControlActions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(all = NotekeaDimensions.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaIconButton(
            icon = if (isPlaying) NotekeaIcons.Pause else NotekeaIcons.Play,
            label = if (isPlaying) labels.pause else labels.play,
            onClick = actions.onPlayToggle
        )

        VideoBar(
            fraction = progress,
            label = labels.seek,
            onChange = actions.onSeek,
            modifier = Modifier.weight(weight = 1f)
        )

        Text(
            text = time(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val isMuted = volume() <= 0f

        NotekeaIconButton(
            icon = if (isMuted) NotekeaIcons.VideoMuted else NotekeaIcons.VideoVolume,
            label = if (isMuted) labels.unmute else labels.mute,
            onClick = actions.onMuteToggle
        )

        VideoBar(
            fraction = volume,
            label = labels.volume,
            onChange = actions.onVolumeChange,
            modifier = Modifier.width(width = VOLUME_WIDTH_DP.dp)
        )

        val screenIcon =
            if (isFullscreen) NotekeaIcons.VideoFullscreenExit else NotekeaIcons.VideoFullscreen

        NotekeaIconButton(
            icon = screenIcon,
            label = if (isFullscreen) labels.exitFullscreen else labels.fullscreen,
            onClick = actions.onFullscreenToggle
        )
    }
}

// A fraction as a bar; a click or a drag on it gives the fraction under the pointer.
@Composable
private fun VideoBar(
    fraction: () -> Float,
    label: String,
    onChange: (Float) -> Unit,
    modifier: Modifier
) {
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = modifier
            .height(height = TRACK_TOUCH_DP.dp)
            .semantics { contentDescription = label }
            .pointerInput(onChange) {
                detectTapGestures { offset -> onChange((offset.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(onChange) {
                detectDragGestures { change, _ ->
                    onChange((change.position.x / size.width).coerceIn(0f, 1f))
                }
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
                .width(width = maxWidth * fraction().coerceIn(0f, 1f))
                .height(height = TRACK_DP.dp)
                .clip(shape = MaterialTheme.shapes.extraSmall)
                .background(color = colors.primary)
        )
    }
}

@Preview
@Composable
private fun NotekeaVideoControlsPreview() {
    NotekeaPreviewThemes {
        NotekeaVideoControls(
            isPlaying = true,
            isFullscreen = false,
            progress = { PREVIEW_PROGRESS },
            time = { PREVIEW_TIME },
            volume = { PREVIEW_VOLUME },
            labels = VideoFrameLabels(
                play = "Play",
                pause = "Pause",
                seek = "Position",
                mute = "Mute",
                unmute = "Unmute",
                volume = "Volume",
                fullscreen = "Full screen",
                exitFullscreen = "Exit full screen"
            ),
            actions = VideoControlActions(
                onPlayToggle = {},
                onSeek = {},
                onVolumeChange = {},
                onMuteToggle = {},
                onFullscreenToggle = {}
            ),
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}

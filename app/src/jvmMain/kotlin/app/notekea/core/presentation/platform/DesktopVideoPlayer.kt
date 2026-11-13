package app.notekea.core.presentation.platform

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaVideoFrame
import app.notekea.core.presentation.designsystem.components.attachments.VideoFrameLabels
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.video_close
import app.notekea.resources.video_pause
import app.notekea.resources.video_play
import app.notekea.resources.video_position
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import org.jetbrains.compose.resources.stringResource
import java.io.File

// The player's position runs from 0 to this, as its slider does.
private const val SLIDER_END = 1000f

/**
 * The video at [location] played by the system's player (ComposeMediaPlayer: Media Foundation, AVFoundation,
 * GStreamer) from its start, let go with its memory when the frame closes or leaves the screen; [onCannotPlay] when
 * this system cannot play its format (a WebM on macOS, H.264 on Linux without GStreamer's plugins).
 */
@Composable
fun DesktopVideoPlayer(
    location: String,
    onClose: () -> Unit,
    onCannotPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = rememberVideoPlayerState()
    val error = player.error

    LaunchedEffect(location) { player.openUri(File(location).absolutePath) }

    LaunchedEffect(error) {
        if (error != null) onCannotPlay()
    }

    NotekeaVideoFrame(
        isPlaying = player.isPlaying,
        progress = {
            player.sliderPos / SLIDER_END
        },
        time = { "${player.positionText} / ${player.durationText}" },
        labels = VideoFrameLabels(
            play = stringResource(resource = Res.string.video_play),
            pause = stringResource(resource = Res.string.video_pause),
            seek = stringResource(resource = Res.string.video_position),
            close = stringResource(resource = Res.string.video_close)
        ),
        onPlayToggle = {
            if (player.isPlaying) player.pause() else player.play()
        },
        onSeek = { player.seekTo(it * SLIDER_END) },
        onClose = onClose,
        modifier = modifier
    ) {
        VideoPlayerSurface(
            playerState = player,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// The frame as the player fills it, without starting a real player in the preview.
@Preview
@Composable
private fun DesktopVideoPlayerPreview() {
    NotekeaPreviewThemes {
        NotekeaVideoFrame(
            isPlaying = false,
            progress = { PREVIEW_PROGRESS },
            time = { PREVIEW_TIME },
            labels = VideoFrameLabels(
                play = stringResource(resource = Res.string.video_play),
                pause = stringResource(resource = Res.string.video_pause),
                seek = stringResource(resource = Res.string.video_position),
                close = stringResource(resource = Res.string.video_close)
            ),
            onPlayToggle = {},
            onSeek = {},
            onClose = {}
        ) {}
    }
}

private const val PREVIEW_PROGRESS = 0.3f
private const val PREVIEW_TIME = "00:36 / 02:00"

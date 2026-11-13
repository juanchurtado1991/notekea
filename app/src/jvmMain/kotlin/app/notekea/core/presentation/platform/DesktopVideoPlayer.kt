package app.notekea.core.presentation.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaVideoControls
import app.notekea.core.presentation.designsystem.components.attachments.NotekeaVideoFrame
import app.notekea.core.presentation.designsystem.components.attachments.VideoControlActions
import app.notekea.core.presentation.designsystem.components.attachments.VideoFrameLabels
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.video_fullscreen
import app.notekea.resources.video_fullscreen_exit
import app.notekea.resources.video_mute
import app.notekea.resources.video_pause
import app.notekea.resources.video_play
import app.notekea.resources.video_position
import app.notekea.resources.video_unmute
import app.notekea.resources.video_volume
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import org.jetbrains.compose.resources.stringResource
import java.io.File

// The player's position runs from 0 to this, as its slider does.
private const val SLIDER_END = 1000f
private const val FULL_VOLUME = 1f

/**
 * The video at [location] played by the system's player (ComposeMediaPlayer: Media Foundation, AVFoundation,
 * GStreamer) from its start, with its volume and full screen (a window over the whole screen, Esc leaves it), let go
 * with its memory when it leaves the screen; [onCannotPlay] when this system cannot play its format.
 */
@Composable
fun DesktopVideoPlayer(
    location: String,
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
        controls = { Controls(player = player) },
        modifier = modifier
    ) {
        VideoPlayerSurface(
            playerState = player,
            modifier = Modifier.fillMaxSize(),
            overlay = { FullscreenControls(player = player) }
        )
    }
}

// Over the whole screen the controls sit at the foot of the video, as under it in the tab.
@Composable
private fun FullscreenControls(player: VideoPlayerState) {
    if (!player.isFullscreen) return

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) { Controls(player = player) }
}

@Composable
private fun Controls(player: VideoPlayerState) {
    // The volume to come back to when the sound is put on again.
    var heard by remember(player) { mutableFloatStateOf(value = FULL_VOLUME) }

    NotekeaVideoControls(
        isPlaying = player.isPlaying,
        isFullscreen = player.isFullscreen,
        progress = {
            player.sliderPos / SLIDER_END
        },
        time = { "${player.positionText} / ${player.durationText}" },
        volume = { player.volume },
        labels = labels(),
        actions = VideoControlActions(
            onPlayToggle = {
                if (player.isPlaying) player.pause() else player.play()
            },
            onSeek = { player.seekTo(it * SLIDER_END) },
            onVolumeChange = { player.volume = it },
            onMuteToggle = {
                if (player.volume > 0f) {
                    heard = player.volume
                    player.volume = 0f
                } else {
                    player.volume = heard
                }
            },
            onFullscreenToggle = { player.toggleFullscreen() }
        )
    )
}

@Composable
private fun labels(): VideoFrameLabels = VideoFrameLabels(
    play = stringResource(resource = Res.string.video_play),
    pause = stringResource(resource = Res.string.video_pause),
    seek = stringResource(resource = Res.string.video_position),
    mute = stringResource(resource = Res.string.video_mute),
    unmute = stringResource(resource = Res.string.video_unmute),
    volume = stringResource(resource = Res.string.video_volume),
    fullscreen = stringResource(resource = Res.string.video_fullscreen),
    exitFullscreen = stringResource(resource = Res.string.video_fullscreen_exit)
)

// The frame as the player fills it, without starting a real player in the preview.
@Preview
@Composable
private fun DesktopVideoPlayerPreview() {
    NotekeaPreviewThemes {
        NotekeaVideoFrame(controls = {}) {}
    }
}

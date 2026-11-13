package app.notekea.shell.presentation.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.core.presentation.platform.LocalYouTubePlayer
import app.notekea.core.presentation.platform.LocalYouTubeView
import app.notekea.shell.domain.PlayedVideo

/**
 * The YouTube tab (the developer's request of 2026-11-13: a tab, not a window of another app): the [video] played by
 * YouTube's own player over the whole tab, with its volume and full screen. The player leaves with the tab.
 */
@Composable
internal fun YouTubeTab(video: PlayedVideo?) {
    val view = LocalYouTubeView.current
    val isCovered = LocalWindowDialogs.current.shown != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.scrim)
    ) {
        if (view != null && video != null) {
            // Another video in the tab starts a player of its own.
            key(video.id) { view(video.id, isCovered, Modifier.fillMaxSize()) }
        }
    }
}

/**
 * Inside [content], a YouTube card's play opens the YouTube tab ([onPlay], with the video's id and title) where this
 * system plays videos in the window ([LocalYouTubeView]); elsewhere it stays what it was, a window of its own.
 */
@Composable
internal fun WithYouTubeTab(
    onPlay: (id: String, title: String) -> Unit,
    content: @Composable () -> Unit
) {
    if (LocalYouTubeView.current == null) return content()

    CompositionLocalProvider(
        LocalYouTubePlayer provides onPlay,
        content = content
    )
}

@Preview
@Composable
private fun WithYouTubeTabPreview() {
    NotekeaPreviewThemes {
        WithYouTubeTab(onPlay = { _, _ -> }) { YouTubeTab(video = null) }
    }
}

@Preview
@Composable
private fun YouTubeTabPreview() {
    NotekeaPreviewThemes {
        YouTubeTab(
            video = PlayedVideo(
                id = "dQw4w9WgXcQ",
                title = "Cómo hacer arepas"
            )
        )
    }
}

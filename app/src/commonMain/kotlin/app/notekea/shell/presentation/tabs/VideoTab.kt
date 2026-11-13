package app.notekea.shell.presentation.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.LocalVideoPlayer
import app.notekea.resources.Res
import app.notekea.resources.video_tab_cannot_play
import org.jetbrains.compose.resources.stringResource

/**
 * A video's tab (the developer's request of 2026-11-13): the video at [location] played over the whole tab, with its
 * volume and full screen. One this system cannot play, or where there is no player, says so over its [details],
 * which open it with its app.
 */
@Composable
internal fun VideoTab(
    path: RelativePath,
    location: String,
    details: @Composable () -> Unit
) {
    val player = LocalVideoPlayer.current
    var cannotPlay by remember(location) { mutableStateOf(value = false) }

    if (player != null && !cannotPlay) {
        // Another video in this tab's place starts its own player.
        key(path) {
            player(
                location,
                { cannotPlay = true },
                Modifier.fillMaxSize()
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            CannotPlay()
            details()
        }
    }
}

@Composable
private fun CannotPlay() {
    Text(
        text = stringResource(resource = Res.string.video_tab_cannot_play),
        modifier = Modifier.padding(
            start = NotekeaDimensions.SpacingXxl,
            top = NotekeaDimensions.SpacingL,
            end = NotekeaDimensions.SpacingXxl
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun VideoTabPreview() {
    NotekeaPreviewThemes {
        VideoTab(
            path = RelativePath(value = "attachments/trip.mp4"),
            location = "attachments/trip.mp4",
            details = {}
        )
    }
}

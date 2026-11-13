package app.notekea.core.presentation.designsystem.components.attachments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private const val PREVIEW_WIDTH_DP = 520
private const val PREVIEW_HEIGHT_DP = 340

/**
 * A video playing in its tab: the [video] over all the room there is, dark behind it, and its [controls] under it
 * ([NotekeaVideoControls]).
 */
@Composable
fun NotekeaVideoFrame(
    controls: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    video: @Composable () -> Unit
) {
    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceContainer)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
                .background(color = MaterialTheme.colorScheme.scrim)
        ) { video() }

        controls()
    }
}

@Preview
@Composable
private fun NotekeaVideoFramePreview() {
    NotekeaPreviewThemes {
        NotekeaVideoFrame(
            controls = {},
            modifier = Modifier
                .width(width = PREVIEW_WIDTH_DP.dp)
                .height(height = PREVIEW_HEIGHT_DP.dp)
        ) {}
    }
}

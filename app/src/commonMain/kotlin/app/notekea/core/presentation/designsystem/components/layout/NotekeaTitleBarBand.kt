package app.notekea.core.presentation.designsystem.components.layout

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalTitleBarSpace

/**
 * The strip a column leaves under the system's title bar when the content runs under it (macOS, redesign 1A): its
 * window buttons sit there, and dragging it moves the window; its width is the [modifier]'s. Nothing where the system
 * draws its own title bar.
 */
@Composable
fun NotekeaTitleBarBand(modifier: Modifier = Modifier) {
    val titleBar = LocalTitleBarSpace.current ?: return

    titleBar.dragArea(modifier.height(height = titleBar.height))
}

@Preview
@Composable
private fun NotekeaTitleBarBandPreview() {
    NotekeaPreviewThemes { NotekeaTitleBarBand() }
}

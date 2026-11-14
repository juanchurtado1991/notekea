package app.notekea.video.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import java.awt.Frame
import java.awt.Window

/**
 * The YouTube video [id] playing inside Notekea's window on macOS, over the room [modifier] gives: the system's web
 * view placed over it ([MacYouTubeSession]) and moved with it; hidden while the window [isCovered] by a dialog, which
 * a native view would otherwise cover. It leaves the window with this composable.
 */
@Composable
fun MacYouTubeView(
    id: String,
    isCovered: Boolean,
    modifier: Modifier = Modifier
) {
    val session = remember(id) {
        MacYouTubeSession(
            id = id,
            windowTitle = ::notekeaWindowTitle
        )
    }

    var bounds by remember { mutableStateOf<Rect?>(value = null) }
    val contentHeight = LocalWindowInfo.current.containerSize.height

    DisposableEffect(session) {
        session.open()

        onDispose { session.close() }
    }

    LaunchedEffect(session, bounds, contentHeight) {
        val shown = bounds ?: return@LaunchedEffect
        val scale = screenScale()

        session.place(
            place = PlayerPlace.of(
                left = shown.left,
                top = shown.top,
                width = shown.width,
                height = shown.height,
                scale = scale
            ),
            contentHeight = contentHeight / scale
        )
    }

    LaunchedEffect(session, isCovered) { session.hide(isHidden = isCovered) }

    Box(modifier = modifier.onGloballyPositioned { bounds = it.boundsInWindow() })
}

// The room the player takes, without starting a real one in the preview.
@Preview
@Composable
private fun MacYouTubeViewPreview() {
    NotekeaPreviewThemes { Box(modifier = Modifier.size(size = PREVIEW_SIZE_DP.dp)) }
}

private const val PREVIEW_SIZE_DP = 240

// The title of the window Notekea shows now, by which the player finds it among the program's windows.
private fun notekeaWindowTitle(): String? = Window
    .getWindows()
    .filterIsInstance<Frame>()
    .firstOrNull { it.isShowing }
    ?.title
    ?.takeIf { it.isNotBlank() }

// The pixels a point of the window's screen: the window in use, or else any that shows.
private fun screenScale(): Double {
    val windows = Window.getWindows().filter { it.isShowing }
    val window = windows.firstOrNull { it.isActive } ?: windows.firstOrNull()

    return window
        ?.graphicsConfiguration
        ?.defaultTransform
        ?.scaleX
        ?: 1.0
}

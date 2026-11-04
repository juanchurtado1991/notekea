package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import app.notekea.core.appearance.domain.UiScale
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.zoomInShortcut
import app.notekea.core.platform.domain.zoomOutShortcut
import app.notekea.core.platform.domain.zoomResetShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import org.koin.compose.viewmodel.koinViewModel

private const val PREVIEW_TEXT = "Zoomed text"

/**
 * [content], the whole window, drawn at the app's zoom (`ROADMAP.md` 1.14): the platform's zoom shortcuts make it
 * larger, smaller or back to the system's size, one [UiScale] step at a time. [UiZoomViewModel] holds the zoom and keeps
 * it. [LocalUiZoom] tells the screens the zoom and how to reset it.
 */
@Composable
fun UiZoom(
    platformConventions: PlatformConventions,
    viewModel: UiZoomViewModel = koinViewModel(),
    content: @Composable () -> Unit
) {
    val scale by viewModel.state.collectAsState()
    ZoomedContent(
        scale = scale,
        platformConventions = platformConventions,
        onIntent = viewModel::onIntent,
        content = content
    )
}

@Composable
private fun ZoomedContent(
    scale: Float,
    platformConventions: PlatformConventions,
    onIntent: (UiZoomIntent) -> Unit,
    content: @Composable () -> Unit
) {
    WindowShortcut(shortcut = platformConventions.zoomInShortcut) { onIntent(UiZoomIntent.OnLarger) }
    WindowShortcut(shortcut = platformConventions.zoomOutShortcut) { onIntent(UiZoomIntent.OnSmaller) }
    WindowShortcut(shortcut = platformConventions.zoomResetShortcut) { onIntent(UiZoomIntent.OnReset) }
    val system = LocalDensity.current
    val resetHint = platformConventions.zoomResetShortcut.label(operatingSystem = platformConventions.operatingSystem)
    CompositionLocalProvider(
        LocalDensity provides Density(density = system.density * scale, fontScale = system.fontScale),
        LocalUiZoom provides UiZoomState(
            scale = scale,
            onReset = { onIntent(UiZoomIntent.OnReset) },
            resetHint = resetHint
        ),
        content = content
    )
}

/** The window's zoom, for what shows it (the status bar); 100 % outside [UiZoom]. */
val LocalUiZoom = staticCompositionLocalOf { UiZoomState() }

@Preview
@Composable
private fun UiZoomPreview() {
    NotekeaPreviewThemes {
        ZoomedContent(
            scale = UiScale.MAX,
            platformConventions = PreviewPlatformConventions.linux,
            onIntent = {}
        ) { Text(text = PREVIEW_TEXT) }
    }
}

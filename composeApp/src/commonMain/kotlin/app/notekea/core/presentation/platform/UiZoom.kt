package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

private const val PREVIEW_TEXT = "Zoomed text"

/**
 * [content], the whole window, drawn at the app's zoom (`ROADMAP.md` 1.14): the platform's zoom shortcuts make it
 * larger, smaller or back to the system's size, one [UiScale] step at a time. The zoom starts where [loadScale] says
 * it was left and each change goes to [saveScale]. [LocalUiZoom] tells the screens the zoom and how to reset it.
 */
@Composable
fun UiZoom(
    platformConventions: PlatformConventions,
    loadScale: suspend () -> Float,
    saveScale: suspend (Float) -> Unit,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(value = UiScale.DEFAULT) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scale = loadScale() }
    val change = { next: Float ->
        scale = next
        scope.launch { saveScale(next) }
    }
    WindowShortcut(shortcut = platformConventions.zoomInShortcut) { change(UiScale.larger(scale = scale)) }
    WindowShortcut(shortcut = platformConventions.zoomOutShortcut) { change(UiScale.smaller(scale = scale)) }
    WindowShortcut(shortcut = platformConventions.zoomResetShortcut) { change(UiScale.DEFAULT) }
    val system = LocalDensity.current
    val resetHint = platformConventions.zoomResetShortcut.label(operatingSystem = platformConventions.operatingSystem)
    CompositionLocalProvider(
        LocalDensity provides Density(density = system.density * scale, fontScale = system.fontScale),
        LocalUiZoom provides UiZoomState(scale = scale, onReset = { change(UiScale.DEFAULT) }, resetHint = resetHint),
        content = content
    )
}

/** The window's zoom, for what shows it (the status bar); 100 % outside [UiZoom]. */
val LocalUiZoom = staticCompositionLocalOf { UiZoomState() }

@Preview
@Composable
private fun UiZoomPreview() {
    NotekeaPreviewThemes {
        UiZoom(
            platformConventions = PreviewPlatformConventions.linux,
            loadScale = { UiScale.MAX },
            saveScale = {}
        ) { Text(text = PREVIEW_TEXT) }
    }
}

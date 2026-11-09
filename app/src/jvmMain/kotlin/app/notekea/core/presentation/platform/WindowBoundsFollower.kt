package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import app.notekea.core.appearance.domain.WindowBounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Dimension
import kotlin.math.roundToInt

private const val SAVE_DELAY_MILLIS = 500L

/**
 * The window can not go below [WindowSizes.MINIMUM], and where it is left is kept once it stays still for a moment, so
 * dragging its edge does not write the settings file on every step.
 */
@Composable
fun FrameWindowScope.FollowWindowBounds(
    state: WindowState,
    viewModel: WindowBoundsViewModel = koinViewModel()
) {
    LaunchedEffect(window) {
        val minimum = WindowSizes.MINIMUM
        window.minimumSize = Dimension(minimum.width.value.roundToInt(), minimum.height.value.roundToInt())
    }
    LaunchedEffect(state) {
        snapshotFlow {
            windowBoundsIntent(placement = state.placement, size = state.size, position = state.position)
        }.collectLatest { intent ->
            delay(timeMillis = SAVE_DELAY_MILLIS)
            intent?.let(viewModel::onIntent)
        }
    }
}

/** What the window's state says happened; nothing in full screen, which never outlives a run. */
fun windowBoundsIntent(
    placement: WindowPlacement,
    size: DpSize,
    position: WindowPosition
): WindowBoundsIntent? = when (placement) {
    WindowPlacement.Maximized -> WindowBoundsIntent.OnMaximized

    WindowPlacement.Fullscreen -> null

    WindowPlacement.Floating -> WindowBoundsIntent.OnFloating(
        bounds = WindowBounds(
            widthDp = size.width.value.roundToInt(),
            heightDp = size.height.value.roundToInt(),
            xDp = position.x.value.takeIf { position.isSpecified }?.roundToInt(),
            yDp = position.y.value.takeIf { position.isSpecified }?.roundToInt(),
            isMaximized = false
        )
    )
}

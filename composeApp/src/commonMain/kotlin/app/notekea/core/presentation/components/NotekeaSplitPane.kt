package app.notekea.core.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val HANDLE_WIDTH_DP = 8
private const val PREVIEW_END = "Editor"
private const val PREVIEW_HEIGHT_DP = 160
private const val PREVIEW_START = "Sidebar"
private const val PREVIEW_START_WIDTH_DP = 140
private const val PREVIEW_WIDTH_DP = 420

/**
 * Two panes side by side with a draggable edge: [start] (the sidebar) at [startWidth] between [minStartWidth] and
 * [maxStartWidth], and [end] filling the rest. The edge follows the pointer while dragging and reports the final width
 * through [onStartWidthChange] once, when the drag ends. Hiding the start pane ([isStartVisible]) slides it closed in
 * the panel motion time; its content keeps its width meanwhile, so nothing reflows while it closes.
 */
@Composable
fun NotekeaSplitPane(
    startWidth: Dp,
    onStartWidthChange: (Dp) -> Unit,
    isStartVisible: Boolean,
    minStartWidth: Dp,
    maxStartWidth: Dp,
    start: @Composable () -> Unit,
    end: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedWidth by remember(startWidth) { mutableStateOf(value = startWidth) }
    var isDragging by remember { mutableStateOf(value = false) }
    val shownWidth by animateDpAsState(
        targetValue = if (isStartVisible) draggedWidth else 0.dp,
        animationSpec = if (isDragging) snap() else tween(durationMillis = NotekeaDimensions.MOTION_PANEL_MILLIS)
    )
    val density = LocalDensity.current
    val dragState = rememberDraggableState { delta ->
        val dragged = draggedWidth + with(density) { delta.toDp() }
        draggedWidth = dragged.coerceIn(minimumValue = minStartWidth, maximumValue = maxStartWidth)
    }
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.width(width = shownWidth).fillMaxHeight().clipToBounds(),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(modifier = Modifier.requiredWidth(width = draggedWidth).fillMaxHeight()) { start() }
        }
        if (isStartVisible) {
            ResizeHandle(
                modifier = Modifier.draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { isDragging = true },
                    onDragStopped = {
                        isDragging = false
                        onStartWidthChange(draggedWidth)
                    }
                )
            )
        }
        Box(modifier = Modifier.weight(weight = 1f).fillMaxHeight()) { end() }
    }
}

@Composable
private fun ResizeHandle(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .width(width = HANDLE_WIDTH_DP.dp)
            .fillMaxHeight()
            .pointerHoverIcon(icon = LocalResizePointerIcon.current),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(width = NotekeaDimensions.BorderWidth)
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.outline)
        )
    }
}

@Preview
@Composable
private fun NotekeaSplitPanePreview() {
    NotekeaPreviewThemes {
        NotekeaSplitPane(
            startWidth = PREVIEW_START_WIDTH_DP.dp,
            onStartWidthChange = {},
            isStartVisible = true,
            minStartWidth = PREVIEW_START_WIDTH_DP.dp,
            maxStartWidth = PREVIEW_WIDTH_DP.dp,
            start = { Text(text = PREVIEW_START) },
            end = { Text(text = PREVIEW_END) },
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

@Preview
@Composable
private fun NotekeaSplitPaneHiddenStartPreview() {
    NotekeaPreviewThemes {
        NotekeaSplitPane(
            startWidth = PREVIEW_START_WIDTH_DP.dp,
            onStartWidthChange = {},
            isStartVisible = false,
            minStartWidth = PREVIEW_START_WIDTH_DP.dp,
            maxStartWidth = PREVIEW_WIDTH_DP.dp,
            start = { Text(text = PREVIEW_START) },
            end = { Text(text = PREVIEW_END) },
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

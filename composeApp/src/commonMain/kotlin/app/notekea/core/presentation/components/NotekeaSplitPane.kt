package app.notekea.core.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val HANDLE_WIDTH_DP = 8
private const val OVERLAY_SHADOW_DP = 8
private const val PREVIEW_CONTENT = "Editor"
private const val PREVIEW_HEIGHT_DP = 160
private const val PREVIEW_PANEL = "Sidebar"
private const val PREVIEW_PANEL_WIDTH_DP = 140
private const val PREVIEW_WIDTH_DP = 420

/**
 * A [panel] beside the [content] with a draggable edge: the panel at [panelWidth] between [minPanelWidth] and
 * [maxPanelWidth] on [panelSide] (the sidebar at the start, the side panel at the end), and the content filling the
 * rest. The edge follows the pointer while dragging and reports the final width through [onPanelWidthChange] once,
 * when the drag ends. Hiding the panel ([isPanelVisible]) slides it closed in the panel motion time; its content keeps
 * its width meanwhile, so nothing reflows while it closes. With [isOverlay] (no room for both) the panel floats over
 * the content with a shadow instead, and cannot be resized. The content stays the same composition in every case, so
 * hiding the panel or resizing the window keeps its state (the editor's scroll, its focus).
 */
@Composable
fun NotekeaSplitPane(
    panelWidth: Dp,
    onPanelWidthChange: (Dp) -> Unit,
    isPanelVisible: Boolean,
    minPanelWidth: Dp,
    maxPanelWidth: Dp,
    panel: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    panelSide: PaneSide = PaneSide.START,
    isOverlay: Boolean = false
) {
    var draggedWidth by remember(panelWidth) { mutableStateOf(value = panelWidth) }
    var isDragging by remember { mutableStateOf(value = false) }
    val shownWidth by animateDpAsState(
        targetValue = if (isPanelVisible) draggedWidth else 0.dp,
        animationSpec = if (isDragging) snap() else tween(durationMillis = NotekeaDimensions.MOTION_PANEL_MILLIS)
    )
    val isStart = panelSide == PaneSide.START
    val dragState = rememberPanelDrag(isStart = isStart, width = { draggedWidth }) {
        draggedWidth = it.coerceIn(minimumValue = minPanelWidth, maximumValue = maxPanelWidth)
    }
    val hasHandle = isPanelVisible && !isOverlay
    val handle = Modifier.draggable(
        state = dragState,
        orientation = Orientation.Horizontal,
        onDragStarted = { isDragging = true },
        onDragStopped = {
            isDragging = false
            onPanelWidthChange(draggedWidth)
        }
    )
    SplitLayout(
        isStart = isStart,
        docked = if (isOverlay) 0.dp else shownWidth + if (hasHandle) HANDLE_WIDTH_DP.dp else 0.dp,
        modifier = modifier,
        panel = {
            PanelBox(
                shownWidth = shownWidth,
                fullWidth = draggedWidth,
                isStart = isStart,
                isOverlay = isOverlay && isPanelVisible,
                panel = panel
            )
        },
        content = content,
        handle = handle.takeIf { hasHandle }
    )
}

/**
 * The drag on the panel's edge: [onDrag] gets the panel's new [width] as the pointer moves. Dragging towards the
 * content widens a panel at the start ([isStart]) and narrows one at the end.
 */
@Composable
private fun rememberPanelDrag(
    isStart: Boolean,
    width: () -> Dp,
    onDrag: (Dp) -> Unit
): DraggableState {
    val density = LocalDensity.current
    val direction = if (isStart) 1 else -1
    return rememberDraggableState { delta -> onDrag(width() + with(density) { (delta * direction).toDp() }) }
}

/**
 * The [content] filling the pane less the [docked] width on the panel's side, and over it the [panel] with its resize
 * [handle] (null when it cannot be resized) between them.
 */
@Composable
private fun SplitLayout(
    isStart: Boolean,
    docked: Dp,
    modifier: Modifier,
    panel: @Composable () -> Unit,
    content: @Composable () -> Unit,
    handle: Modifier?
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(
                start = if (isStart) docked else 0.dp,
                end = if (isStart) 0.dp else docked
            )
        ) { content() }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(alignment = if (isStart) Alignment.TopStart else Alignment.TopEnd)
        ) {
            if (!isStart) handle?.let { ResizeHandle(modifier = it) }
            panel()
            if (isStart) handle?.let { ResizeHandle(modifier = it) }
        }
    }
}

/** The panel at [shownWidth] while it slides, laid out at [fullWidth] and clipped; with a shadow when it floats. */
@Composable
private fun PanelBox(
    shownWidth: Dp,
    fullWidth: Dp,
    isStart: Boolean,
    isOverlay: Boolean,
    panel: @Composable () -> Unit
) {
    val floating = if (isOverlay) {
        Modifier.shadow(elevation = OVERLAY_SHADOW_DP.dp).background(color = MaterialTheme.colorScheme.surface)
    } else {
        Modifier
    }
    Box(
        modifier = Modifier.width(width = shownWidth).fillMaxHeight().then(other = floating).clipToBounds(),
        contentAlignment = if (isStart) Alignment.TopEnd else Alignment.TopStart
    ) {
        Box(modifier = Modifier.requiredWidth(width = fullWidth).fillMaxHeight()) { panel() }
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
            // The panes' surface, so the handle reads as their hairline and not as a gap down to the window's background.
            .background(color = MaterialTheme.colorScheme.surface)
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

@Composable
private fun SplitPanePreviewFrame(
    isPanelVisible: Boolean = true,
    panelSide: PaneSide = PaneSide.START,
    isOverlay: Boolean = false
) {
    NotekeaPreviewThemes {
        NotekeaSplitPane(
            panelWidth = PREVIEW_PANEL_WIDTH_DP.dp,
            onPanelWidthChange = {},
            isPanelVisible = isPanelVisible,
            minPanelWidth = PREVIEW_PANEL_WIDTH_DP.dp,
            maxPanelWidth = PREVIEW_WIDTH_DP.dp,
            panel = { Text(text = PREVIEW_PANEL) },
            content = { Text(text = PREVIEW_CONTENT) },
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp),
            panelSide = panelSide,
            isOverlay = isOverlay
        )
    }
}

@Preview
@Composable
private fun NotekeaSplitPanePreview() {
    SplitPanePreviewFrame()
}

@Preview
@Composable
private fun NotekeaSplitPaneHiddenPanelPreview() {
    SplitPanePreviewFrame(isPanelVisible = false)
}

@Preview
@Composable
private fun NotekeaSplitPaneEndPreview() {
    SplitPanePreviewFrame(panelSide = PaneSide.END)
}

@Preview
@Composable
private fun NotekeaSplitPaneOverlayPreview() {
    SplitPanePreviewFrame(panelSide = PaneSide.END, isOverlay = true)
}

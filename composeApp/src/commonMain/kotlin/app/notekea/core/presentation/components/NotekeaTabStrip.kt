package app.notekea.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.notekea.core.presentation.platform.onMiddleClick
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val ACCENT_HEIGHT_DP = 2
private const val LABEL_MAX_WIDTH_DP = 180
private const val PREVIEW_CLOSE = "Close tab"
private const val SETTLE_MILLIS = 500L
private const val STRIP_HEIGHT_DP = 34
private val PREVIEW_TABS = listOf(
    NotekeaTabStripItem(key = "a", label = "Pricing ideas", icon = NotekeaIcons.Note),
    NotekeaTabStripItem(key = "b", label = "2026-10-23", icon = NotekeaIcons.Note),
    NotekeaTabStripItem(key = "c", label = "plate.png", icon = NotekeaIcons.ImageFile)
)

/**
 * A row of document tabs above an editor pane (`ROADMAP.md` 1.11), as browsers and code editors show them: each with
 * its icon, its label cut with an ellipsis when long, and a close button; the [selectedIndex] one sits on the
 * surface, with an accent line on top in the primary color while its pane is [isActive]. A click selects a tab, the
 * middle button or the close button closes it ([onClose]), a right click opens [menuOf]'s sections. A tab dragged along
 * the row stays on its line while the others slide aside to open its place, and once let go it glides into that place
 * ([onReorder] with its new index among the others); dragged off the row, it follows the pointer, and once let go
 * reports where in the window ([onDrop]). The row scrolls sideways when the tabs do
 * not fit; [trailing] (a "+" button) follows the last tab. Tabs are not focus stops: the keyboard moves through them
 * with the editor's shortcuts.
 */
@Composable
fun NotekeaTabStrip(
    tabs: List<NotekeaTabStripItem>,
    selectedIndex: Int,
    isActive: Boolean,
    closeLabel: String,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    onDrop: (index: Int, windowPosition: Offset) -> Unit,
    onReorder: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    menuOf: (Int) -> List<List<NotekeaMenuItem>> = { emptyList() },
    trailing: @Composable () -> Unit = {}
) {
    val targets = remember { TabDropTargets() }
    val keys = tabs.map { it.key }
    val drag = targets.dragOf(keys = keys)
    val release = rememberTabRelease(targets = targets, onReorder = onReorder, onDrop = onDrop)
    SettleTabDrag(targets = targets, keys = keys)
    Column(modifier = modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = STRIP_HEIGHT_DP.dp)
                .onGloballyPositioned { targets.row = it.boundsInWindow() }
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom
        ) {
            tabs.forEachIndexed { index, tab ->
                key(tab.key) {
                    DraggableTab(
                        tab = tab,
                        index = index,
                        isSelected = index == selectedIndex,
                        isActive = isActive,
                        closeLabel = closeLabel,
                        targets = targets,
                        sections = menuOf(index),
                        onSelect = { onSelect(index) },
                        onClose = { onClose(index) },
                        onDrag = { start -> targets.drag = TabDrag(index = index, keys = keys, start = start) },
                        onRelease = release,
                        drag = drag
                    )
                }
            }
            Box(modifier = Modifier.height(height = STRIP_HEIGHT_DP.dp), contentAlignment = Alignment.Center) {
                trailing()
            }
        }
        StripBorder()
    }
}

/** The line under the tabs, that the selected tab's surface meets. */
@Composable
private fun StripBorder() {
    Box(
        modifier = Modifier.fillMaxWidth().height(height = NotekeaDimensions.BorderWidth).background(
            color = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

/** The tab at [index] with its menu, drawn where the drag in [targets] puts it, above the others while dragged. */
@Composable
private fun DraggableTab(
    tab: NotekeaTabStripItem,
    index: Int,
    isSelected: Boolean,
    isActive: Boolean,
    closeLabel: String,
    targets: TabDropTargets,
    sections: List<List<NotekeaMenuItem>>,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onDrag: (start: Offset) -> Unit,
    onRelease: () -> Unit,
    drag: TabDrag?
) {
    val offset = tabOffset(index = index, targets = targets, drag = drag)
    NotekeaContextMenuArea(
        sections = sections,
        modifier = Modifier
            .zIndex(zIndex = if (drag?.index == index) 1f else 0f)
            .onGloballyPositioned { targets.places[index] = it.boundsInWindow() },
        takesFocus = false
    ) {
        StripTab(
            tab = tab,
            isSelected = isSelected,
            isActive = isActive,
            closeLabel = closeLabel,
            offset = offset,
            onSelect = onSelect,
            onClose = onClose,
            onDrag = onDrag,
            onMove = { travel -> targets.drag?.travel = travel },
            onRelease = onRelease,
            onCancel = { targets.drag = null }
        )
    }
}

/**
 * Where the tab at [index] is drawn while a tab is dragged: the dragged one where [TabDropTargets.offsetOf] puts it,
 * the others sliding aside to open its place.
 */
@Composable
private fun tabOffset(
    index: Int,
    targets: TabDropTargets,
    drag: TabDrag?
): () -> Offset {
    val isDragged = drag?.index == index
    val shift by animateFloatAsState(
        targetValue = if (drag == null || isDragged) 0f else targets.shiftOf(index = index, drag = drag),
        animationSpec = tween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
    )
    return {
        when {
            drag == null -> Offset.Zero
            isDragged -> targets.offsetOf(drag = drag)
            else -> Offset(x = shift, y = 0f)
        }
    }
}

/**
 * What happens when the dragged tab is let go: off the row it goes to [onDrop] at once; on the row it glides into its
 * place, which goes to [onReorder] when it is a new one.
 */
@Composable
private fun rememberTabRelease(
    targets: TabDropTargets,
    onReorder: (from: Int, to: Int) -> Unit,
    onDrop: (index: Int, windowPosition: Offset) -> Unit
): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentOnReorder by rememberUpdatedState(newValue = onReorder)
    val currentOnDrop by rememberUpdatedState(newValue = onDrop)
    return remember(targets, scope) {
        release@{
            val drag = targets.drag ?: return@release
            if (!targets.row.contains(offset = drag.pointer)) {
                targets.drag = null
                currentOnDrop(drag.index, drag.pointer)
                return@release
            }
            val slot = targets.slotOf(drag = drag)
            drag.landing = slot
            scope.launch {
                Animatable(initialValue = drag.travel, typeConverter = Offset.VectorConverter).animateTo(
                    targetValue = Offset(x = targets.glideTo(slot = slot, drag = drag), y = 0f),
                    animationSpec = tween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
                ) { drag.travel = value }
                if (slot == drag.index) targets.drag = null else currentOnReorder(drag.index, slot)
            }
        }
    }
}

/**
 * Lets the drag go once the tabs took their new order ([keys] changed), or after a while if they never do, so the tabs
 * slid aside go back to their places.
 */
@Composable
private fun SettleTabDrag(
    targets: TabDropTargets,
    keys: List<Any>
) {
    val landing = targets.drag?.landing
    LaunchedEffect(keys, landing) {
        if (targets.drag?.keys != keys) {
            targets.drag = null
        } else if (landing != null) {
            delay(timeMillis = SETTLE_MILLIS)
            targets.drag = null
        }
    }
}

@Composable
private fun StripTab(
    tab: NotekeaTabStripItem,
    isSelected: Boolean,
    isActive: Boolean,
    closeLabel: String,
    offset: () -> Offset,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onDrag: (start: Offset) -> Unit,
    onMove: (travel: Offset) -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var place by remember { mutableStateOf<LayoutCoordinates?>(value = null) }
    val accent = when {
        isSelected && isActive -> colors.primary
        isSelected -> colors.outline
        else -> colors.background
    }
    Column(
        modifier = Modifier
            // As wide as its label, so the accent line on top spans the tab inside the scrolling row.
            .width(intrinsicSize = IntrinsicSize.Max)
            .offset { offset().let { IntOffset(x = it.x.roundToInt(), y = it.y.roundToInt()) } }
            .onGloballyPositioned { place = it }
            .background(color = if (isSelected) colors.surface else colors.background)
            .semantics { selected = isSelected }
            .onMiddleClick(onClick = onClose)
            .tabDrag(
                start = { pressed -> place?.localToWindow(relativeToLocal = pressed) ?: Offset.Zero },
                onStart = { start ->
                    onSelect()
                    onDrag(start)
                },
                onMove = onMove,
                onRelease = onRelease,
                onCancel = onCancel
            )
            .focusProperties { canFocus = false }
            .clickable(role = Role.Tab, onClick = onSelect)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(height = ACCENT_HEIGHT_DP.dp).background(color = accent))
        TabLabel(tab = tab, isSelected = isSelected, closeLabel = closeLabel, onClose = onClose)
    }
}

/** The tab's icon, its label cut with an ellipsis when long, and its close button. */
@Composable
private fun TabLabel(
    tab: NotekeaTabStripItem,
    isSelected: Boolean,
    closeLabel: String,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(
            start = NotekeaDimensions.SpacingS,
            end = NotekeaDimensions.SpacingXs,
            top = NotekeaDimensions.SpacingXs,
            bottom = NotekeaDimensions.SpacingXs
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tab.icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingXs))
        }
        Text(
            text = tab.label,
            modifier = Modifier.widthIn(max = LABEL_MAX_WIDTH_DP.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        NotekeaIconButton(icon = NotekeaIcons.Close, label = closeLabel, onClick = onClose, takesFocus = false)
    }
}

/**
 * A tab dragged reports where the press was in the window ([start], measured from where the tab is laid out, because the
 * strip clips a tab dragged out of it, so its own bounds cannot say), how far it went ([onMove]) and when it is let go
 * ([onRelease]) or the drag is cancelled ([onCancel]). The drag outlives recompositions (the tab moves on every step), so it reads the latest callbacks.
 */
@Composable
private fun Modifier.tabDrag(
    start: (pressed: Offset) -> Offset,
    onStart: (Offset) -> Unit,
    onMove: (Offset) -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit
): Modifier {
    val currentStart by rememberUpdatedState(newValue = start)
    val currentOnStart by rememberUpdatedState(newValue = onStart)
    val currentOnMove by rememberUpdatedState(newValue = onMove)
    val currentOnRelease by rememberUpdatedState(newValue = onRelease)
    val currentOnCancel by rememberUpdatedState(newValue = onCancel)
    return pointerInput(Unit) {
        var moved = Offset.Zero
        detectDragGestures(
            onDragStart = { pressed ->
                moved = Offset.Zero
                currentOnStart(currentStart(pressed))
            },
            onDragEnd = { currentOnRelease() },
            onDragCancel = { currentOnCancel() }
        ) { change, amount ->
            change.consume()
            moved += amount
            currentOnMove(moved)
        }
    }
}

@Preview
@Composable
private fun NotekeaTabStripPreview() {
    NotekeaPreviewThemes {
        NotekeaTabStrip(
            tabs = PREVIEW_TABS,
            selectedIndex = 1,
            isActive = true,
            closeLabel = PREVIEW_CLOSE,
            onSelect = {},
            onClose = {},
            onDrop = { _, _ -> },
            onReorder = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun NotekeaTabStripInactivePreview() {
    NotekeaPreviewThemes {
        NotekeaTabStrip(
            tabs = PREVIEW_TABS,
            selectedIndex = 0,
            isActive = false,
            closeLabel = PREVIEW_CLOSE,
            onSelect = {},
            onClose = {},
            onDrop = { _, _ -> },
            onReorder = { _, _ -> }
        )
    }
}

package app.notekea.core.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.notekea.core.presentation.platform.onMiddleClick
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import kotlin.math.roundToInt

private const val HOVER_ALPHA = 0.5f
private const val LABEL_MAX_WIDTH_DP = 180
private const val UNSAVED_DOT_DP = 8
private const val SEPARATOR_HEIGHT_DP = 16
private const val TAB_HEIGHT_DP = 30
private const val TAB_MIN_WIDTH_DP = 96
private const val TAB_RADIUS_DP = 8

/** The tab at [index] with its menu, drawn where the drag in [targets] puts it, above the others while dragged. */
@Composable
internal fun DraggableTab(
    tab: NotekeaTabStripItem,
    index: Int,
    isSelected: Boolean,
    isActive: Boolean,
    hasSeparator: Boolean,
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
            hasSeparator = hasSeparator,
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
 * One tab: on the surface with rounded top corners while [isSelected], a faint fill while hovered, quiet otherwise, a
 * thin line at its end ([hasSeparator]) while neither it nor its neighbour is the selected one.
 */
@Composable
private fun StripTab(
    tab: NotekeaTabStripItem,
    isSelected: Boolean,
    isActive: Boolean,
    hasSeparator: Boolean,
    closeLabel: String,
    offset: () -> Offset,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onDrag: (start: Offset) -> Unit,
    onMove: (travel: Offset) -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit
) {
    var place by remember { mutableStateOf<LayoutCoordinates?>(value = null) }
    val hover = remember { MutableInteractionSource() }
    val isHovered by hover.collectIsHoveredAsState()
    val shape = RoundedCornerShape(topStart = TAB_RADIUS_DP.dp, topEnd = TAB_RADIUS_DP.dp)
    Row(
        modifier = Modifier
            .height(height = TAB_HEIGHT_DP.dp)
            .widthIn(min = TAB_MIN_WIDTH_DP.dp)
            .offset { offset().let { IntOffset(x = it.x.roundToInt(), y = it.y.roundToInt()) } }
            .onGloballyPositioned { place = it }
            .clip(shape = shape)
            .background(color = tabColor(isSelected = isSelected, isHovered = isHovered))
            .hoverable(interactionSource = hover)
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
            .clickable(role = Role.Tab, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabLabel(
            tab = tab,
            look = TabLook(isSelected = isSelected, isActive = isActive, isHovered = isHovered),
            closeLabel = closeLabel,
            onClose = onClose
        )
        Separator(isShown = hasSeparator && !isHovered)
    }
}

/** The selected tab's surface, a faint fill under the pointer, or nothing. */
@Composable
private fun tabColor(
    isSelected: Boolean,
    isHovered: Boolean
): Color = when {
    isSelected -> MaterialTheme.colorScheme.surface
    isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = HOVER_ALPHA)
    else -> Color.Transparent
}

/** The thin line between two quiet tabs. */
@Composable
private fun Separator(
    isShown: Boolean
) {
    Box(
        modifier = Modifier
            .width(width = NotekeaDimensions.BorderWidth)
            .height(height = SEPARATOR_HEIGHT_DP.dp)
            .background(color = if (isShown) MaterialTheme.colorScheme.outline else Color.Transparent)
    )
}

/**
 * The tab's icon (primary in the active pane's selected tab), its label cut with an ellipsis when long (semibold when
 * selected), and its close button or unsaved dot.
 */
@Composable
private fun TabLabel(
    tab: NotekeaTabStripItem,
    look: TabLook,
    closeLabel: String,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(start = NotekeaDimensions.SpacingM, end = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tab.icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = if (look.isSelected && look.isActive) colors.primary else tab.iconTint ?: colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingXs))
        }
        Text(
            text = tab.label,
            modifier = Modifier.widthIn(max = LABEL_MAX_WIDTH_DP.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (look.isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (look.isSelected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CloseOrDot(unsavedLabel = tab.unsavedLabel, look = look, closeLabel = closeLabel, onClose = onClose)
    }
}

/**
 * The tab's close button, seen on the selected or hovered tab and kept in place otherwise; a tab with unsaved changes
 * shows a dot there instead, as VS Code does, until the pointer is on it.
 */
@Composable
private fun CloseOrDot(
    look: TabLook,
    closeLabel: String,
    onClose: () -> Unit,
    unsavedLabel: String?
) {
    Box(contentAlignment = Alignment.Center) {
        val isDotShown = unsavedLabel != null && !look.isHovered
        NotekeaIconButton(
            icon = NotekeaIcons.Close,
            label = closeLabel,
            onClick = onClose,
            modifier = Modifier.alpha(alpha = if (!isDotShown && (look.isSelected || look.isHovered)) 1f else 0f),
            takesFocus = false
        )
        if (isDotShown) {
            Box(
                modifier = Modifier
                    .size(size = UNSAVED_DOT_DP.dp)
                    .background(color = MaterialTheme.colorScheme.onSurfaceVariant, shape = CircleShape)
                    .semantics { contentDescription = unsavedLabel.orEmpty() }
            )
        }
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
private fun DraggableTabPreview() {
    NotekeaPreviewThemes {
        DraggableTab(
            tab = NotekeaTabStripItem(key = PREVIEW_KEY, label = PREVIEW_LABEL, icon = NotekeaIcons.Note),
            index = 0,
            isSelected = true,
            isActive = true,
            hasSeparator = false,
            closeLabel = PREVIEW_CLOSE,
            targets = TabDropTargets(),
            sections = emptyList(),
            onSelect = {},
            onClose = {},
            onDrag = {},
            onRelease = {},
            drag = null
        )
    }
}

private const val PREVIEW_CLOSE = "Close tab"
private const val PREVIEW_KEY = "a"
private const val PREVIEW_LABEL = "Pricing ideas"

package app.notekea.core.presentation.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.roundToInt

private const val ACCENT_HEIGHT_DP = 2
private const val LABEL_MAX_WIDTH_DP = 180
private const val PREVIEW_CLOSE = "Close tab"
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
 * middle button or the close button closes it ([onClose]), a right click opens [menuOf]'s sections, and a tab dragged
 * and dropped reports where the pointer was let go in the window ([onDrop]). The row scrolls sideways when the tabs do
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
    modifier: Modifier = Modifier,
    menuOf: (Int) -> List<List<NotekeaMenuItem>> = { emptyList() },
    trailing: @Composable () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(
                height = STRIP_HEIGHT_DP.dp
            ).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom
        ) {
            tabs.forEachIndexed { index, tab ->
                NotekeaContextMenuArea(sections = menuOf(index), takesFocus = false) {
                    StripTab(
                        tab = tab,
                        isSelected = index == selectedIndex,
                        isActive = isActive,
                        closeLabel = closeLabel,
                        onSelect = { onSelect(index) },
                        onClose = { onClose(index) },
                        onDrop = { onDrop(index, it) }
                    )
                }
            }
            Box(modifier = Modifier.height(height = STRIP_HEIGHT_DP.dp), contentAlignment = Alignment.Center) {
                trailing()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = NotekeaDimensions.BorderWidth)
                .background(color = MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun StripTab(
    tab: NotekeaTabStripItem,
    isSelected: Boolean,
    isActive: Boolean,
    closeLabel: String,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onDrop: (Offset) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var dragged by remember { mutableStateOf(value = Offset.Zero) }
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
            .zIndex(zIndex = if (dragged == Offset.Zero) 0f else 1f)
            .offset { IntOffset(x = dragged.x.roundToInt(), y = dragged.y.roundToInt()) }
            .onGloballyPositioned { place = it }
            .background(color = if (isSelected) colors.surface else colors.background)
            .semantics { selected = isSelected }
            .onMiddleClick(onClick = onClose)
            .tabDrag(place = { place }, onMove = { dragged = it }, onStart = onSelect, onDrop = onDrop)
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
 * A tab follows the pointer while dragged ([onMove] gets how far it went) and, once let go, reports where in the
 * window ([onDrop]): the press's place there, measured from [place], plus the drag, because the strip clips a tab
 * dragged out of it, so its own bounds cannot say. The drag outlives recompositions (the tab moves on every step), so it
 * reads the latest callbacks.
 */
@Composable
private fun Modifier.tabDrag(
    place: () -> LayoutCoordinates?,
    onMove: (Offset) -> Unit,
    onStart: () -> Unit,
    onDrop: (Offset) -> Unit
): Modifier {
    val currentOnStart by rememberUpdatedState(newValue = onStart)
    val currentOnDrop by rememberUpdatedState(newValue = onDrop)
    val currentOnMove by rememberUpdatedState(newValue = onMove)
    return pointerInput(Unit) {
        var start = Offset.Zero
        var moved = Offset.Zero
        detectDragGestures(
            onDragStart = { pressed ->
                start = place()?.localToWindow(relativeToLocal = pressed) ?: Offset.Zero
                moved = Offset.Zero
                currentOnStart()
            },
            onDragEnd = {
                currentOnDrop(start + moved)
                currentOnMove(Offset.Zero)
            },
            onDragCancel = { currentOnMove(Offset.Zero) }
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
            onDrop = { _, _ -> }
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
            onDrop = { _, _ -> }
        )
    }
}

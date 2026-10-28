package app.notekea.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FADE_WIDTH_DP = 40
private const val PREVIEW_CLOSE = "Close tab"
private const val PREVIEW_UNSAVED = "Unsaved changes"
private const val SETTLE_MILLIS = 500L
private const val STRIP_HEIGHT_DP = 34

private val PREVIEW_TABS = listOf(
    NotekeaTabStripItem(key = "a", label = "Pricing ideas", icon = NotekeaIcons.Note),
    NotekeaTabStripItem(key = "b", label = "2026-10-23", icon = NotekeaIcons.Note, unsavedLabel = PREVIEW_UNSAVED),
    NotekeaTabStripItem(key = "c", label = "plate.png", icon = NotekeaIcons.ImageFile)
)

/**
 * A row of document tabs above an editor pane (`ROADMAP.md` 1.11), as browsers and code editors show them: each with
 * its icon, its label cut with an ellipsis when long, and a close button; the [selectedIndex] one rises with rounded
 * top corners into the pane's surface, its icon in the primary color while its pane is [isActive], and the others stay
 * quiet on the strip, a faint fill under the pointer, a thin line between them, their close button shown on hover. A click selects a tab, the
 * middle button or the close button closes it ([onClose]), a right click opens [menuOf]'s sections. A tab dragged along
 * the row stays on its line while the others slide aside to open its place, and once let go it glides into that place
 * ([onReorder] with its new index among the others); dragged off the row, it follows the pointer, and once let go
 * reports where in the window ([onDrop]). The row scrolls sideways when the tabs do not fit, fading out at its end
 * while there is more, and a ⌄ button named [allTabsLabel] lists every tab to pick one; [trailing] (a "+" button)
 * follows the last tab. Tabs are not focus stops: the keyboard moves through them
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
    allTabsLabel: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    val targets = remember { TabDropTargets() }
    val keys = tabs.map { it.key }
    val release = rememberTabRelease(targets = targets, onReorder = onReorder, onDrop = onDrop)
    SettleTabDrag(targets = targets, keys = keys)
    val scroll = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = STRIP_HEIGHT_DP.dp)
            .background(color = MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(weight = 1f)) {
            ScrollingTabs(
                tabs = tabs,
                look = StripLook(selectedIndex = selectedIndex, isActive = isActive, closeLabel = closeLabel),
                targets = targets,
                scroll = scroll,
                menuOf = menuOf,
                onSelect = onSelect,
                onClose = onClose,
                release = release,
                trailing = trailing
            )
            if (scroll.canScrollForward) OverflowFade(modifier = Modifier.align(alignment = Alignment.CenterEnd))
        }
        if (allTabsLabel != null && (scroll.canScrollForward || scroll.canScrollBackward)) {
            AllTabsButton(tabs = tabs, selectedIndex = selectedIndex, label = allTabsLabel, onSelect = onSelect)
        }
    }
}

/** The tabs in their scrolling row, the [trailing] button after the last one. */
@Composable
private fun ScrollingTabs(
    tabs: List<NotekeaTabStripItem>,
    look: StripLook,
    targets: TabDropTargets,
    scroll: ScrollState,
    menuOf: (Int) -> List<List<NotekeaMenuItem>>,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    release: () -> Unit,
    trailing: @Composable () -> Unit
) {
    val keys = tabs.map { it.key }
    val drag = targets.dragOf(keys = keys)
    val selectedIndex = look.selectedIndex
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = STRIP_HEIGHT_DP.dp)
            .onGloballyPositioned { targets.row = it.boundsInWindow() }
            .horizontalScroll(state = scroll),
        verticalAlignment = Alignment.Bottom
    ) {
        tabs.forEachIndexed { index, tab ->
            key(tab.key) {
                DraggableTab(
                    tab = tab,
                    index = index,
                    isSelected = index == selectedIndex,
                    isActive = look.isActive,
                    hasSeparator =
                    index != selectedIndex && index + 1 != selectedIndex && index < tabs.lastIndex,
                    closeLabel = look.closeLabel,
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
}

/** The strip's background fading in over the tabs' end, where more tabs are out of view. */
@Composable
private fun OverflowFade(
    modifier: Modifier
) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .width(width = FADE_WIDTH_DP.dp)
            .height(height = STRIP_HEIGHT_DP.dp)
            .background(brush = Brush.horizontalGradient(colors = listOf(background.copy(alpha = 0f), background)))
    )
}

/** ⌄: the menu of every open tab, the selected one checked; picking one selects it. */
@Composable
private fun AllTabsButton(
    tabs: List<NotekeaTabStripItem>,
    selectedIndex: Int,
    label: String,
    onSelect: (Int) -> Unit
) {
    NotekeaMenuButton(
        icon = NotekeaIcons.Collapse,
        label = label,
        sections = listOf(
            tabs.mapIndexed { index, tab ->
                NotekeaMenuItem(
                    label = tab.label,
                    onClick = { onSelect(index) },
                    icon = tab.icon,
                    isChecked = index == selectedIndex
                )
            }
        ),
        takesFocus = false
    )
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

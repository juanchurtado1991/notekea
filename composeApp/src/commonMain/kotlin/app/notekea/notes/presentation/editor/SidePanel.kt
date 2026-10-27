package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaCompactTabs
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.SidePanelTab
import app.notekea.resources.Res
import app.notekea.resources.outline_empty_message
import app.notekea.resources.outline_empty_title
import app.notekea.resources.outline_label
import app.notekea.resources.outline_tab
import app.notekea.resources.properties_title
import app.notekea.resources.side_panel_hide
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 360
private const val PREVIEW_WIDTH_DP = 300
private val LIST_PADDING = PaddingValues(all = NotekeaDimensions.SpacingXs)

/**
 * The panel on the right of the editor (`ROADMAP.md` 1.9), with its tabs (Outline and Properties for now; Related,
 * Backlinks and Chat come later), [selectedTab] shown and a click on another going to [onTabSelect], and a button that
 * hides it. The Properties tab shows [properties] (`ROADMAP.md` 1.14). The Outline tab lists the note's headings as an indented tree, highlights
 * [current], the one the editor's view is in, and goes to a heading when it is clicked; a note without headings says
 * how to add one. The list is one focus stop, taken with [outlineFocus] (`Mod+Shift+O`): the arrows move, Enter goes to
 * the heading and Esc gives the keyboard back to the note ([onLeave]).
 */
@Composable
fun SidePanel(
    outline: List<OutlineHeading>,
    current: Int,
    selectedTab: SidePanelTab,
    platformConventions: PlatformConventions,
    onHeadingClick: (Int) -> Unit,
    onTabSelect: (SidePanelTab) -> Unit,
    onHide: () -> Unit,
    onLeave: () -> Unit,
    properties: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    outlineFocus: FocusRequester = remember { FocusRequester() }
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column {
            SidePanelHeader(
                selectedTab = selectedTab,
                platformConventions = platformConventions,
                onTabSelect = onTabSelect,
                onHide = onHide
            )
            if (selectedTab == SidePanelTab.PROPERTIES) {
                properties()
            } else if (outline.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    EmptyState(
                        title = stringResource(resource = Res.string.outline_empty_title),
                        message = stringResource(resource = Res.string.outline_empty_message)
                    )
                }
            } else {
                OutlineList(
                    outline = outline,
                    current = current,
                    focusRequester = outlineFocus,
                    onHeadingClick = onHeadingClick,
                    onLeave = onLeave
                )
            }
        }
    }
}

@Composable
private fun SidePanelHeader(
    selectedTab: SidePanelTab,
    platformConventions: PlatformConventions,
    onTabSelect: (SidePanelTab) -> Unit,
    onHide: () -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            start = NotekeaDimensions.SpacingM,
            top = NotekeaDimensions.SpacingS,
            end = NotekeaDimensions.SpacingXs
        ),
        verticalAlignment = Alignment.Bottom
    ) {
        NotekeaCompactTabs(
            labels = listOf(
                stringResource(resource = Res.string.outline_tab),
                stringResource(resource = Res.string.properties_title)
            ),
            selectedIndex = SidePanelTab.entries.indexOf(element = selectedTab),
            onSelect = { onTabSelect(SidePanelTab.entries[it]) },
            shortcutHints = listOf(platformConventions.outlineShortcut.label(operatingSystem = operatingSystem)),
            takesFocus = false
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        NotekeaIconButton(
            icon = NotekeaIcons.HideSidePanel,
            label = stringResource(resource = Res.string.side_panel_hide),
            onClick = onHide,
            shortcutHint = platformConventions.toggleSidePanelShortcut.label(operatingSystem = operatingSystem),
            takesFocus = false
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outlineVariant)
    )
}

/**
 * The headings, indented by their depth. A row a click chose leaves the keyboard where it was (the editor takes it in
 * Edit mode); the keyboard's own row starts at the current heading each time the list takes focus.
 */
@Composable
private fun OutlineList(
    outline: List<OutlineHeading>,
    current: Int,
    focusRequester: FocusRequester,
    onHeadingClick: (Int) -> Unit,
    onLeave: () -> Unit
) {
    val listState = rememberLazyListState()
    var hasFocus by remember { mutableStateOf(value = false) }
    var focused by remember { mutableIntStateOf(value = 0) }
    val label = stringResource(resource = Res.string.outline_label)
    FollowCurrent(listState = listState, index = if (hasFocus) focused else current)
    // Each time the list takes the keyboard, its row starts at the current heading.
    val onFocusChange = { isFocused: Boolean ->
        if (isFocused && !hasFocus) focused = current.coerceIn(minimumValue = 0, maximumValue = outline.lastIndex)
        hasFocus = isFocused
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = label }
            .outlineInput(focusRequester = focusRequester, onFocusChange = onFocusChange) { event ->
                outlineKey(
                    event = event,
                    focused = focused,
                    count = outline.size,
                    onMove = { focused = it },
                    onChoose = { onHeadingClick(focused) },
                    onLeave = onLeave
                )
            },
        state = listState,
        contentPadding = LIST_PADDING
    ) {
        itemsIndexed(items = outline) { index, heading ->
            NotekeaTreeItem(
                label = heading.text,
                icon = null,
                depth = heading.depth,
                onClick = { onHeadingClick(index) },
                selected = index == current,
                focused = hasFocus && index == focused
            )
        }
    }
}

/** The outline is one focus stop that handles its own keys ([onKey]); [onFocusChange] says whether it has focus. */
private fun Modifier.outlineInput(
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    onKey: (KeyEvent) -> Boolean
): Modifier = focusRequester(focusRequester = focusRequester)
    .onFocusChanged { onFocusChange(it.hasFocus) }
    .focusable()
    .onKeyEvent(onKeyEvent = onKey)

/** Keeps the row at [index] (the current heading, or the keyboard's row) in view when it changes. */
@Composable
private fun FollowCurrent(
    listState: LazyListState,
    index: Int
) {
    LaunchedEffect(key1 = index) {
        val isVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == index && it.offset >= 0 }
        if (index >= 0 && !isVisible) listState.animateScrollToItem(index = index)
    }
}

/**
 * The outline's keys: Up, Down, Home and End move the keyboard's row ([onMove]); Enter chooses it ([onChoose]) and Esc
 * leaves the list ([onLeave]). Returns whether the key was handled, so other keys reach the window's shortcuts.
 */
private fun outlineKey(
    event: KeyEvent,
    focused: Int,
    count: Int,
    onMove: (Int) -> Unit,
    onChoose: () -> Unit,
    onLeave: () -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown || count == 0) return false
    val move = when (event.key) {
        Key.DirectionUp -> focused - 1
        Key.DirectionDown -> focused + 1
        Key.MoveHome -> 0
        Key.MoveEnd -> count - 1
        else -> null
    }
    val action = when (event.key) {
        Key.Enter, Key.NumPadEnter -> onChoose
        Key.Escape -> onLeave
        else -> null
    }
    move?.let { onMove(it.coerceIn(minimumValue = 0, maximumValue = count - 1)) }
    action?.invoke()
    return move != null || action != null
}

@Composable
private fun SidePanelPreviewFrame(
    outline: List<OutlineHeading>,
    current: Int
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            SidePanel(
                outline = outline,
                current = current,
                selectedTab = SidePanelTab.OUTLINE,
                platformConventions = EditorPreviewData.platformConventions,
                onHeadingClick = {},
                onTabSelect = {},
                onHide = {},
                onLeave = {},
                properties = {}
            )
        }
    }
}

@Preview
@Composable
private fun SidePanelPreview() {
    SidePanelPreviewFrame(outline = EditorPreviewData.outline, current = 1)
}

@Preview
@Composable
private fun SidePanelEmptyPreview() {
    SidePanelPreviewFrame(outline = emptyList(), current = OutlineTracker.NONE)
}

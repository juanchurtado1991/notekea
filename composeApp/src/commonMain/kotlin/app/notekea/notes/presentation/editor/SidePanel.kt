package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaTooltip
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.SidePanelSection
import app.notekea.resources.Res
import app.notekea.resources.note_info_title
import app.notekea.resources.outline_empty_message
import app.notekea.resources.outline_empty_title
import app.notekea.resources.outline_label
import app.notekea.resources.outline_title
import app.notekea.resources.side_panel_hide
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 480
private const val PREVIEW_WIDTH_DP = 300
private const val SECTION_LETTER_SPACING_SP = 0.8
private val LIST_PADDING = PaddingValues(all = NotekeaDimensions.SpacingXs)

/**
 * The panel on the right of the editor (`ROADMAP.md` 1.9), an inspector of foldable sections ([foldedSections] are
 * folded; a header's click goes to [onSectionToggle]), with a button that hides it on the first. Outline lists the
 * note's headings as an indented tree, highlights [current], the one the editor's view is in, and goes to a heading when
 * it is clicked; a note without headings says how to add one. The list is one focus stop, taken with [outlineFocus]
 * (`Mod+Shift+O`): the arrows move, Enter goes to the heading and Esc gives the keyboard back to the note ([onLeave]).
 * Note info, shown while a note is open ([info]), says what it holds and when it was created and modified.
 */
@Composable
fun SidePanel(
    outline: List<OutlineHeading>,
    current: Int,
    foldedSections: Set<SidePanelSection>,
    platformConventions: PlatformConventions,
    actions: SidePanelActions,
    info: NoteInfo?,
    modifier: Modifier = Modifier,
    outlineFocus: FocusRequester = remember { FocusRequester() }
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(vertical = NotekeaDimensions.SpacingXs)) {
            val isOutlineOpen = SidePanelSection.OUTLINE !in foldedSections
            SectionHeader(
                title = stringResource(resource = Res.string.outline_title),
                isOpen = isOutlineOpen,
                onToggle = { actions.onSectionToggle(SidePanelSection.OUTLINE) },
                shortcutHint = platformConventions.outlineShortcut.label(
                    operatingSystem = platformConventions.operatingSystem
                )
            ) { HideButton(platformConventions = platformConventions, onHide = actions.onHide) }
            if (isOutlineOpen) {
                OutlineSection(
                    outline = outline,
                    current = current,
                    focusRequester = outlineFocus,
                    actions = actions,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
            }
            info?.let { NoteInfoSection(info = it, isOpen = SidePanelSection.NOTE_INFO !in foldedSections, actions) }
        }
    }
}

/** The outline's headings, or how to add one when the note has none. */
@Composable
private fun OutlineSection(
    outline: List<OutlineHeading>,
    current: Int,
    focusRequester: FocusRequester,
    actions: SidePanelActions,
    modifier: Modifier
) {
    if (outline.isEmpty()) {
        Column(
            modifier = Modifier.padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingXs
            ),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            Text(
                text = stringResource(resource = Res.string.outline_empty_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(resource = Res.string.outline_empty_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        OutlineList(
            outline = outline,
            current = current,
            focusRequester = focusRequester,
            onHeadingClick = actions.onHeadingClick,
            onLeave = actions.onLeave,
            modifier = modifier
        )
    }
}

/**
 * A section's header, as desktop inspectors draw it: a chevron and its [title] in small capitals, a click folding or
 * opening it ([onToggle]), and [trailing] at the end. Its tooltip names the [shortcutHint] that goes to it, if any.
 */
@Composable
private fun SectionHeader(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    shortcutHint: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            start = NotekeaDimensions.SpacingXs,
            end = NotekeaDimensions.SpacingXs
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(weight = 1f)) {
            NotekeaTooltip(label = title, shortcutHint = shortcutHint) {
                SectionTitle(title = title, isOpen = isOpen, onToggle = onToggle)
            }
        }
        trailing()
    }
}

/** The chevron and the title in small capitals; a click folds or opens the section. */
@Composable
private fun SectionTitle(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = NotekeaDimensions.CompactControlSize)
            .clip(shape = MaterialTheme.shapes.small)
            .focusProperties { canFocus = false }
            .clickable(role = Role.Button, onClick = onToggle)
            .padding(horizontal = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Icon(
            imageVector = if (isOpen) NotekeaIcons.Collapse else NotekeaIcons.Expand,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
            tint = muted
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = SECTION_LETTER_SPACING_SP.sp
            ),
            color = muted
        )
    }
}

@Composable
private fun HideButton(
    platformConventions: PlatformConventions,
    onHide: () -> Unit
) {
    NotekeaIconButton(
        icon = NotekeaIcons.HideSidePanel,
        label = stringResource(resource = Res.string.side_panel_hide),
        onClick = onHide,
        shortcutHint = platformConventions.toggleSidePanelShortcut.label(
            operatingSystem = platformConventions.operatingSystem
        ),
        takesFocus = false
    )
}

/** A hairline between sections, then Note info: its rows while it is open. */
@Composable
private fun NoteInfoSection(
    info: NoteInfo,
    isOpen: Boolean,
    actions: SidePanelActions
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS)
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outline)
    )
    SectionHeader(
        title = stringResource(resource = Res.string.note_info_title),
        isOpen = isOpen,
        onToggle = { actions.onSectionToggle(SidePanelSection.NOTE_INFO) }
    )
    if (isOpen) NoteInfoRows(info = info)
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
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
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
    current: Int,
    foldedSections: Set<SidePanelSection> = emptySet()
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            SidePanel(
                outline = outline,
                current = current,
                foldedSections = foldedSections,
                platformConventions = EditorPreviewData.platformConventions,
                actions = SidePanelActions(onHeadingClick = {}, onSectionToggle = {}, onHide = {}, onLeave = {}),
                info = EditorPreviewData.info
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

@Preview
@Composable
private fun SidePanelFoldedPreview() {
    SidePanelPreviewFrame(
        outline = EditorPreviewData.outline,
        current = 1,
        foldedSections = setOf(SidePanelSection.OUTLINE)
    )
}

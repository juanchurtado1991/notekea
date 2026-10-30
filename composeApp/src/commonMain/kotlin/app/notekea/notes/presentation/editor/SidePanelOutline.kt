package app.notekea.notes.presentation.editor

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import app.notekea.core.presentation.components.NotekeaLazyScrollArea
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.OutlineHeading
import app.notekea.resources.Res
import app.notekea.resources.outline_empty_message
import app.notekea.resources.outline_empty_title
import app.notekea.resources.outline_label
import org.jetbrains.compose.resources.stringResource

private val LIST_PADDING = PaddingValues(all = NotekeaDimensions.SpacingXs)

/** The outline's headings, or how to add one when the note has none. */
@Composable
internal fun OutlineSection(
    outline: List<OutlineHeading>,
    current: Int,
    focusRequester: FocusRequester,
    actions: SidePanelActions,
    modifier: Modifier = Modifier
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
    NotekeaLazyScrollArea(state = listState, modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label }
                .outlineInput(focusRequester = focusRequester, onFocusChange = onFocusChange) { event ->
                    outlineKey(event, focused, outline.size, { focused = it }, { onHeadingClick(focused) }, onLeave)
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

@Preview
@Composable
private fun OutlineSectionPreview() {
    NotekeaPreviewThemes {
        Column {
            OutlineSection(
                outline = EditorPreviewData.outline,
                current = 1,
                focusRequester = remember { FocusRequester() },
                actions = SidePanelActions(
                    onHeadingClick = {},
                    onModeSelect = {},
                    onSectionToggle = {},
                    onHide = {},
                    onLeave = {}
                )
            )
        }
    }
}

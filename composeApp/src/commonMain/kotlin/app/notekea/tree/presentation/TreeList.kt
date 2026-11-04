package app.notekea.tree.presentation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import app.notekea.core.presentation.components.NotekeaLazyScrollArea
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.isContextMenuKey
import app.notekea.core.presentation.platform.matches
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.tree_new_folder
import app.notekea.resources.tree_new_note_from_template
import app.notekea.resources.tree_new_note
import app.notekea.resources.tree_untitled_folder
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.TreeRow
import androidx.compose.ui.Modifier
import app.notekea.core.presentation.theme.LocalReduceMotion
import app.notekea.core.presentation.theme.followItem
import app.notekea.core.presentation.TestTags

private const val EMPTY_ROWS_BELOW = 2
private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 260

// Room below the last row, so there is always empty space to click or right-click for the root.
private val TREE_PADDING = PaddingValues(
    start = NotekeaDimensions.SpacingXs,
    top = NotekeaDimensions.SpacingXs,
    end = NotekeaDimensions.SpacingXs,
    bottom = NotekeaDimensions.TreeRowHeight * EMPTY_ROWS_BELOW
)

/**
 * The vault tree. It is one focus stop moved with the keyboard: arrows move and open folders, the platform's rename and
 * delete shortcuts act on the selected entry, Enter opens or closes a folder and Shift+F10 or the Menu key opens the
 * context menu. While a name is being edited, keys belong to the field. The list follows the selection when it moves
 * out of view, and focus comes back to the tree when renaming ends. A click on the empty part of the list (there is
 * always some below the last row) clears the selection and a right click there offers New note and New folder, so
 * entries can be created at the vault root.
 */
@Composable
internal fun TreeList(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(value = false) }
    var menuRequest by remember { mutableStateOf<RelativePath?>(value = null) }
    FollowSelection(listState = listState, state = state)
    LaunchedEffect(key1 = state.renaming == null) { if (state.renaming == null) focusRequester.requestFocus() }
    val currentState = rememberUpdatedState(newValue = state)
    val stateNow = remember<() -> VaultTreeState> { { currentState.value } }
    val createItems = rememberCreateItems(state = currentState, onIntent = onIntent)
    val input = Modifier
        .fillMaxSize()
        .treeInput(focusRequester, state, onIntent, { menuRequest = state.selectedPath }) { hasFocus = it }
    NotekeaContextMenuArea(
        menu = { listOf(createItems()) },
        modifier = modifier,
        onOpenByPointer = { onIntent(VaultTreeIntent.OnSelectionClear) }
    ) {
        NotekeaLazyScrollArea(state = listState, modifier = Modifier.fillMaxSize().testTag(tag = TestTags.TREE)) {
            LazyColumn(modifier = input, state = listState, contentPadding = TREE_PADDING) {
                items(items = state.rows, key = { it.path.value }, contentType = ::contentTypeOf) { row ->
                    // A folder's rows fade in as it opens, and the rows below slide down to make room.
                    Box(modifier = if (LocalReduceMotion.current) Modifier else Modifier.animateItem()) {
                        ListedEntry(
                            row = row,
                            state = state,
                            stateNow = stateNow,
                            createItems = createItems,
                            hasFocus = hasFocus,
                            isMenuRequested = menuRequest == row.path,
                            onMenuShown = { menuRequest = null },
                            onIntent = onIntent,
                            onFocusTree = { focusRequester.requestFocus() }
                        )
                    }
                }
            }
        }
    }
}

/** The tree's New note and New folder items, built on the latest [state] when a menu opens. */
@Composable
private fun rememberCreateItems(
    state: State<VaultTreeState>,
    onIntent: (VaultTreeIntent) -> Unit
) = remember<@Composable () -> List<NotekeaMenuItem>>(onIntent) {
    { createMenuItems(state = state.value, onIntent = onIntent) }
}

/** One row of the list, given only what concerns it, so a change to another row leaves it alone. */
@Composable
private fun ListedEntry(
    row: TreeRow,
    state: VaultTreeState,
    stateNow: () -> VaultTreeState,
    createItems: @Composable () -> List<NotekeaMenuItem>,
    hasFocus: Boolean,
    isMenuRequested: Boolean,
    onMenuShown: () -> Unit,
    onIntent: (VaultTreeIntent) -> Unit,
    onFocusTree: () -> Unit
) {
    TreeEntryRow(
        row = row,
        state = stateNow,
        createItems = createItems,
        isSelected = row.path == state.selectedPath,
        isBookmarked = row.path in state.bookmarks,
        isFocused = hasFocus && row.path == state.selectedPath,
        isMenuRequested = isMenuRequested,
        onMenuShown = onMenuShown,
        onIntent = onIntent,
        onFocusTree = onFocusTree,
        draft = state.renaming?.takeIf { it.path == row.path }
    )
}

/**
 * The tree is one focus stop that handles its keys; [onFocusChange] says whether it (or the rename field inside it)
 * has focus. A click that no row took (rows consume their own clicks), that is, on the empty part of the list, clears
 * the selection.
 */
private fun Modifier.treeInput(
    focusRequester: FocusRequester,
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    onMenuRequest: () -> Unit,
    onFocusChange: (Boolean) -> Unit
): Modifier = focusRequester(focusRequester = focusRequester)
    .onFocusChanged { onFocusChange(it.hasFocus) }
    .focusable()
    .onKeyEvent { handleTreeKey(event = it, state = state, onIntent = onIntent, onMenuRequest = onMenuRequest) }
    .pointerInput(onIntent, focusRequester) {
        detectTapGestures {
            onIntent(VaultTreeIntent.OnSelectionClear)
            focusRequester.requestFocus()
        }
    }

private fun contentTypeOf(
    row: TreeRow
): String = if (row is TreeRow.Folder) FOLDER_ROW else NOTE_ROW

@Composable
private fun createMenuItems(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
): List<NotekeaMenuItem> {
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    val untitledFolder = stringResource(resource = Res.string.tree_untitled_folder)
    return listOf(
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.tree_new_note),
            onClick = { onIntent(VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)) },
            icon = NotekeaIcons.NewNote,
            shortcutHint = state.platformConventions.newNoteShortcut.label(
                operatingSystem = state.platformConventions.operatingSystem
            )
        ),
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.tree_new_note_from_template),
            onClick = { onIntent(VaultTreeIntent.OnNewNoteFromTemplateClick) },
            icon = NotekeaIcons.Template
        ),
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.tree_new_folder),
            onClick = { onIntent(VaultTreeIntent.OnNewFolderClick(baseName = untitledFolder)) },
            icon = NotekeaIcons.NewFolder
        )
    )
}

@Composable
private fun FollowSelection(
    listState: LazyListState,
    state: VaultTreeState
) {
    val index = state.rows.indexOfFirst { it.path == state.selectedPath }
    val reduceMotion = LocalReduceMotion.current
    LaunchedEffect(key1 = index) {
        val visible = listState.layoutInfo.visibleItemsInfo
        val isVisible = visible.any { it.index == index && it.offset >= 0 }
        if (index >= 0 && !isVisible) listState.followItem(index = index, reduceMotion = reduceMotion)
    }
}

/** Returns whether the key was handled, so unhandled keys keep moving focus and reach window shortcuts. */
private fun handleTreeKey(
    event: KeyEvent,
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    onMenuRequest: () -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown || state.renaming != null) return false
    val intent = treeIntentFor(event = event, state = state)
    val isMenuKey = event.isContextMenuKey
    if (isMenuKey && state.selectedPath != null) onMenuRequest()
    intent?.let(onIntent)
    return intent != null || isMenuKey
}

private fun treeIntentFor(
    event: KeyEvent,
    state: VaultTreeState
): VaultTreeIntent? {
    val conventions = state.platformConventions
    return when {
        // Before the arrows: on macOS Cmd+Down opens, as in Finder, where Enter renames.
        conventions.openSelectedShortcut.matches(event = event) && state.selectedPath != null ->
            VaultTreeIntent.OnOpenSelected

        event.key == Key.DirectionDown -> VaultTreeIntent.OnSelectNext

        event.key == Key.DirectionUp -> VaultTreeIntent.OnSelectPrevious

        event.key == Key.DirectionRight -> VaultTreeIntent.OnExpandSelected

        event.key == Key.DirectionLeft -> VaultTreeIntent.OnCollapseSelected

        conventions.renameShortcut.matches(event = event) -> VaultTreeIntent.OnRenameRequest()

        conventions.deleteShortcut.matches(event = event) ||
            conventions.alternateDeleteShortcut?.matches(event = event) == true -> VaultTreeIntent.OnDeleteRequest()

        else -> null
    }
}

@Preview
@Composable
private fun TreeListPreview() {
    NotekeaPreviewThemes {
        TreeList(
            state = TreePreviewData.state(),
            onIntent = {},
            modifier = Modifier
                .width(
                    width = PREVIEW_WIDTH_DP.dp
                ).height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

@Preview
@Composable
private fun TreeListRenamingPreview() {
    NotekeaPreviewThemes {
        TreeList(
            state = TreePreviewData.renamingState(),
            onIntent = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

private const val FOLDER_ROW = "folder"
private const val NOTE_ROW = "note"

package app.notekea.notes.presentation.tree

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.notekea.core.presentation.components.NotekeaFold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaSectionHeader
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.tree_bookmark_missing_remove
import app.notekea.resources.tree_bookmark_remove
import app.notekea.resources.tree_bookmarks
import org.jetbrains.compose.resources.stringResource
import app.notekea.notes.presentation.files.fileIconOf
import app.notekea.notes.presentation.files.fileTintOf
import kotlin.math.roundToInt

private const val PREVIEW_WIDTH_DP = 300

/**
 * The vault's bookmarks above the tree (`ROADMAP.md` 1.13), under a BOOKMARKS header that folds them; nothing while
 * there are none. A bookmark opens its entry in a tab and selects it in the tree; dragged up or down, it moves among
 * the others; its menu removes it. One whose entry is no longer in the vault (moved or deleted outside Notekea) is
 * greyed out, with a ✕ that removes it.
 */
@Composable
internal fun BookmarksSection(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val paths = state.bookmarks.paths
    if (paths.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = NotekeaDimensions.SpacingXs)) {
        NotekeaSectionHeader(
            title = stringResource(resource = Res.string.tree_bookmarks),
            isOpen = state.isBookmarksExpanded,
            onToggle = { onIntent(BookmarkIntent.OnSectionToggle) },
            count = paths.size
        )
        NotekeaFold(isOpen = state.isBookmarksExpanded) {
            paths.forEachIndexed { index, path ->
                BookmarkRow(path = path, index = index, count = paths.size, state = state, onIntent = onIntent)
            }
        }
    }
}

/** One bookmark, draggable along the list, with its menu. */
@Composable
private fun BookmarkRow(
    path: RelativePath,
    index: Int,
    count: Int,
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
) {
    val isMissing = !state.isLoading && path !in state.tree
    val menu = listOf(
        listOf(
            NotekeaMenuItem(
                label = stringResource(resource = Res.string.tree_bookmark_remove),
                onClick = { onIntent(BookmarkIntent.OnRemove(path = path)) },
                icon = NotekeaIcons.Bookmark
            )
        )
    )
    NotekeaContextMenuArea(
        sections = menu,
        modifier = Modifier.bookmarkDrag(
            index = index,
            count = count,
            onMove = { from, to -> onIntent(BookmarkIntent.OnMove(from = from, to = to)) }
        )
    ) {
        NotekeaTreeItem(
            label = labelOf(path = path),
            depth = 1,
            onClick = { onIntent(BookmarkIntent.OnClick(path = path)) },
            icon = iconOf(path = path, state = state),
            iconTint = fileTintOf(path = path).takeUnless { path in state.tree.folders },
            selected = path == state.selectedPath,
            labelContent = if (isMissing) {
                { MissingLabel(path = path, onIntent = onIntent) }
            } else {
                null
            }
        )
    }
}

/**
 * A bookmark row follows the pointer up and down while dragged, above the others, and once let go moves to the place
 * it reached among the [count] bookmarks ([onMove] from its [index]), a row height at a time.
 */
@Composable
private fun Modifier.bookmarkDrag(
    index: Int,
    count: Int,
    onMove: (from: Int, to: Int) -> Unit
): Modifier {
    var dragged by remember { mutableFloatStateOf(value = 0f) }
    val rowHeight = with(LocalDensity.current) { NotekeaDimensions.TreeRowHeight.toPx() }
    val currentIndex by rememberUpdatedState(newValue = index)
    val currentCount by rememberUpdatedState(newValue = count)
    val currentOnMove by rememberUpdatedState(newValue = onMove)
    return zIndex(zIndex = if (dragged == 0f) 0f else 1f)
        .graphicsLayer { translationY = dragged }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    val target = (currentIndex + (dragged / rowHeight).roundToInt()).coerceIn(0, currentCount - 1)
                    dragged = 0f
                    if (target != currentIndex) currentOnMove(currentIndex, target)
                },
                onDragCancel = { dragged = 0f }
            ) { change, amount ->
                change.consume()
                dragged += amount.y
            }
        }
}

/** A bookmark whose entry is gone: its name greyed out, and a ✕ that removes it, its tooltip saying why. */
@Composable
private fun MissingLabel(
    path: RelativePath,
    onIntent: (VaultTreeIntent) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = labelOf(path = path),
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = MISSING_ALPHA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        NotekeaIconButton(
            icon = NotekeaIcons.Close,
            label = stringResource(resource = Res.string.tree_bookmark_missing_remove),
            onClick = { onIntent(BookmarkIntent.OnRemove(path = path)) },
            takesFocus = false
        )
    }
}

/** A note by its name without `.md`, a folder or another file by its name. */
private fun labelOf(
    path: RelativePath
): String = if (path.isMarkdown) path.nameWithoutExtension else path.name

/** The icon of what the bookmark points to: a folder, a note, or another file by its kind. */
private fun iconOf(
    path: RelativePath,
    state: VaultTreeState
): ImageVector = when {
    path in state.tree.folders -> NotekeaIcons.Folder
    path.isMarkdown -> NotekeaIcons.Note
    else -> fileIconOf(path = path)
}

private const val MISSING_ALPHA = 0.6f

@Preview
@Composable
private fun BookmarksSectionPreview() {
    NotekeaPreviewThemes {
        BookmarksSection(
            state = TreePreviewData.state().let { state ->
                state.copy(
                    bookmarks = state.bookmarks.toggled(path = state.rows.first().path)
                        .toggled(path = RelativePath(value = PREVIEW_MISSING))
                )
            },
            onIntent = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}

private const val PREVIEW_MISSING = "Old plan.md"

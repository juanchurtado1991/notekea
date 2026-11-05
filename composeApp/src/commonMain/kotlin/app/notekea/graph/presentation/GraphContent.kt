package app.notekea.graph.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaCheckbox
import app.notekea.core.presentation.components.NotekeaCountTabs
import app.notekea.core.presentation.components.NotekeaFindBar
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.editor_find_count
import app.notekea.resources.editor_find_none
import app.notekea.resources.graph_depth_one
import app.notekea.resources.graph_depth_two
import app.notekea.resources.graph_empty_message
import app.notekea.resources.graph_empty_title
import app.notekea.resources.graph_find
import app.notekea.resources.graph_index_not_ready
import app.notekea.resources.graph_no_note_message
import app.notekea.resources.graph_no_note_title
import app.notekea.resources.graph_orphans
import app.notekea.resources.graph_related
import app.notekea.resources.graph_this_note
import app.notekea.resources.graph_too_large
import app.notekea.resources.graph_whole_vault
import org.jetbrains.compose.resources.stringResource

private const val SCOPE_WIDTH_DP = 200
private const val DEPTH_WIDTH_DP = 72
private const val LEGEND_DOT_DP = 8
private const val EXCLUDED_ALPHA = 0.45f
private const val MAX_DEPTH = 2

/**
 * The graph tab (`ROADMAP.md` 2.7): a compact toolbar, the map, the legend of folders, which also leaves a folder out
 * when clicked, and the inspector of the selected note. The keyboard walks the map: arrows move between connected notes,
 * `Enter` opens one, `Space` shows or hides the inspector, `Esc` goes back to the note and `Mod+F` finds a note, with
 * the keyboard back on the map when the search closes. A focused control gets its keys first.
 */
@Composable
fun GraphContent(
    state: GraphState,
    platformConventions: PlatformConventions,
    onIntent: (GraphIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(focus, state.isSearching) { if (!state.isSearching) focus.requestFocus() }
    WindowShortcut(shortcut = platformConventions.findShortcut) { onIntent(GraphIntent.OnSearchOpen) }
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .focusRequester(focusRequester = focus)
            .onKeyEvent { event ->
                event.type == KeyEventType.KeyDown && onKey(event.key, state.isSearching, onIntent)
            }
            .focusable()
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Toolbar(state = state, platformConventions = platformConventions, onIntent = onIntent)
            Box(modifier = Modifier.weight(weight = 1f).fillMaxWidth()) {
                GraphCanvas(state = state, onIntent = onIntent, modifier = Modifier.fillMaxSize())
                if (state.needsNote) {
                    EmptyState(
                        title = stringResource(resource = Res.string.graph_no_note_title),
                        message = stringResource(resource = Res.string.graph_no_note_message),
                        modifier = Modifier.align(alignment = Alignment.Center),
                        action = {
                            NotekeaButton(
                                text = stringResource(resource = Res.string.graph_find),
                                onClick = { onIntent(GraphIntent.OnSearchOpen) }
                            )
                        }
                    )
                }
                if (state.isEmpty) {
                    EmptyState(
                        title = stringResource(resource = Res.string.graph_empty_title),
                        message = stringResource(resource = Res.string.graph_empty_message),
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
                if (state.isSearching) {
                    SearchBar(
                        state = state,
                        onIntent = onIntent,
                        modifier = Modifier.align(alignment = Alignment.TopEnd).padding(all = NotekeaDimensions.SpacingS)
                    )
                }
            }
            Footer(state = state, onIntent = onIntent)
        }
        val preview = state.preview
        if (state.isInspectorOpen && preview != null) {
            GraphInspector(state = state, preview = preview, onIntent = onIntent)
        }
    }
}

private fun onKey(
    key: Key,
    isSearching: Boolean,
    onIntent: (GraphIntent) -> Unit
): Boolean {
    if (isSearching) {
        if (key == Key.Escape) onIntent(GraphIntent.OnSearchClose)
        return key == Key.Escape
    }
    val intent = when (key) {
        Key.DirectionLeft -> GraphIntent.OnMove(direction = GraphDirection.LEFT)
        Key.DirectionRight -> GraphIntent.OnMove(direction = GraphDirection.RIGHT)
        Key.DirectionUp -> GraphIntent.OnMove(direction = GraphDirection.UP)
        Key.DirectionDown -> GraphIntent.OnMove(direction = GraphDirection.DOWN)
        Key.Enter -> GraphIntent.OnOpenSelected
        Key.Spacebar -> GraphIntent.OnInspectorToggle
        Key.Escape -> GraphIntent.OnBack
        else -> return false
    }
    onIntent(intent)
    return true
}

/** Finds notes by name on the map: Enter goes through them, Esc closes. */
@Composable
private fun SearchBar(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit,
    modifier: Modifier
) {
    val label = when {
        state.query.isBlank() -> ""
        state.matches.isEmpty() -> stringResource(resource = Res.string.editor_find_none)
        else -> stringResource(Res.string.editor_find_count, state.matchIndex + 1, state.matches.size)
    }
    NotekeaFindBar(
        query = state.query,
        onQueryChange = { onIntent(GraphIntent.OnQueryChange(query = it)) },
        matchLabel = label,
        onNext = { onIntent(GraphIntent.OnMatchStep(isForward = true)) },
        onPrevious = { onIntent(GraphIntent.OnMatchStep(isForward = false)) },
        onClose = { onIntent(GraphIntent.OnSearchClose) },
        modifier = modifier,
        placeholder = stringResource(resource = Res.string.graph_find)
    )
}

@Composable
private fun Toolbar(
    state: GraphState,
    platformConventions: PlatformConventions,
    onIntent: (GraphIntent) -> Unit
) {
    val isAround = state.scope == GraphScope.THIS_NOTE
    Row(
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScopeTabs(state = state, onIntent = onIntent)
        if (isAround) DepthTabs(state = state, onIntent = onIntent)
        Toggle(
            label = stringResource(if (isAround) Res.string.graph_related else Res.string.graph_orphans),
            isChecked = if (isAround) state.showsRelated else state.showsOrphans,
            onClick = { onIntent(if (isAround) GraphIntent.OnRelatedToggle else GraphIntent.OnOrphansToggle) }
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        NotekeaIconButton(
            icon = NotekeaIcons.Find,
            label = stringResource(resource = Res.string.graph_find),
            onClick = { onIntent(GraphIntent.OnSearchOpen) },
            shortcutHint = platformConventions.findShortcut.label(operatingSystem = platformConventions.operatingSystem)
        )
    }
}

@Composable
private fun ScopeTabs(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit
) {
    NotekeaCountTabs(
        labels = listOf(
            stringResource(resource = Res.string.graph_this_note),
            stringResource(resource = Res.string.graph_whole_vault)
        ),
        counts = listOf(null, null),
        selectedIndex = state.scope.ordinal,
        onSelect = { onIntent(GraphIntent.OnScopeSelect(scope = GraphScope.entries[it])) },
        modifier = Modifier.width(width = SCOPE_WIDTH_DP.dp)
    )
}

@Composable
private fun DepthTabs(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit
) {
    NotekeaCountTabs(
        labels = listOf(
            stringResource(resource = Res.string.graph_depth_one),
            stringResource(resource = Res.string.graph_depth_two)
        ),
        counts = listOf(null, null),
        selectedIndex = (state.depth - 1).coerceIn(minimumValue = 0, maximumValue = MAX_DEPTH - 1),
        onSelect = { onIntent(GraphIntent.OnDepthSelect(depth = it + 1)) },
        modifier = Modifier.width(width = DEPTH_WIDTH_DP.dp)
    )
}

@Composable
private fun Toggle(
    label: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        NotekeaCheckbox(checked = isChecked, onClick = onClick, label = label)
        Text(text = label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}

/** The legend, and what the graph has to say: the index is not ready, or the vault is too large. */
@Composable
private fun Footer(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit
) {
    val message = when {
        state.isTooLarge -> Res.string.graph_too_large
        !state.isIndexReady && state.scope == GraphScope.THIS_NOTE && state.showsRelated ->
            Res.string.graph_index_not_ready
        else -> null
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.folders.forEach { folder ->
            LegendItem(
                folder = folder,
                folders = state.folders,
                isExcluded = folder in state.excludedFolders,
                onClick = { onIntent(GraphIntent.OnFolderToggle(folder = folder)) }
            )
        }
        message?.let {
            Text(
                text = stringResource(resource = it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(
    folder: String,
    folders: List<String>,
    isExcluded: Boolean,
    onClick: () -> Unit
) {
    val color = folderColor(folder, folders, MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Box(
            modifier = Modifier
                .size(size = LEGEND_DOT_DP.dp)
                .background(color = color.copy(alpha = if (isExcluded) EXCLUDED_ALPHA else 1f), shape = CircleShape)
        )
        Text(
            text = folder,
            style = MaterialTheme.typography.bodySmall,
            textDecoration = if (isExcluded) TextDecoration.LineThrough else null,
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun GraphContentPreview() {
    NotekeaPreviewThemes {
        GraphContent(
            state = GraphPreviewData.state.copy(isSearching = true, query = "are"),
            platformConventions = PreviewPlatformConventions.linux,
            onIntent = {}
        )
    }
}

package app.notekea.notes.presentation.switcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaPalette
import app.notekea.core.presentation.components.NotekeaPaletteItem
import app.notekea.core.presentation.components.NotekeaProgressIndicator
import app.notekea.core.presentation.components.NotekeaTab
import app.notekea.core.presentation.components.NotekeaTabRow
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.switcher_content_hint_message
import app.notekea.resources.switcher_content_hint_title
import app.notekea.resources.switcher_empty_vault_message
import app.notekea.resources.switcher_empty_vault_title
import app.notekea.resources.switcher_error
import app.notekea.resources.switcher_mode_content
import app.notekea.resources.switcher_mode_notes
import app.notekea.resources.switcher_no_match_content
import app.notekea.resources.switcher_no_match_notes
import app.notekea.resources.switcher_no_match_title
import app.notekea.resources.switcher_placeholder_content
import app.notekea.resources.switcher_placeholder_link
import app.notekea.resources.switcher_placeholder_notes
import app.notekea.resources.switcher_searching
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 520
private const val PREVIEW_WIDTH_DP = 760
private const val RESULTS_MAX_HEIGHT_DP = 440
private const val ROW_CONTENT_TYPE = "result"

/**
 * The quick switcher palette (`ROADMAP.md` 1.6) over the vault workspace, while [QuickSwitcherState.isOpen]: tabs for
 * names and content (Tab switches), the search field, and the results, the selected one highlighted and kept in view.
 * While a content search runs the previous results stay, with a small indicator by the tabs. Nothing found says
 * "No notes match", an empty content search explains what can be typed, and an empty vault says there are no notes.
 */
@Composable
fun QuickSwitcherContent(
    state: QuickSwitcherState,
    onIntent: (QuickSwitcherIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isOpen) return
    val isLinking = state.purpose == QuickSwitcherPurpose.LINK
    val header: (@Composable () -> Unit)? = if (isLinking) {
        null
    } else {
        { ModeTabs(mode = state.mode, isSearching = state.isSearching, onIntent = onIntent) }
    }
    NotekeaPalette(
        query = state.query,
        onQueryChange = { onIntent(QuickSwitcherIntent.OnQueryChange(query = it)) },
        placeholder = stringResource(resource = placeholderOf(state = state)),
        onMove = { step ->
            onIntent(if (step > 0) QuickSwitcherIntent.OnSelectNext else QuickSwitcherIntent.OnSelectPrevious)
        },
        onConfirm = { onIntent(QuickSwitcherIntent.OnConfirm) },
        onDismiss = { onIntent(QuickSwitcherIntent.OnClose) },
        modifier = modifier,
        onTab = if (isLinking) null else ({ onIntent(QuickSwitcherIntent.OnModeSwitch) }),
        header = header
    ) {
        SwitcherBody(state = state, onIntent = onIntent)
    }
}

/** What the search field invites to type: a note to link, a note name, or text inside notes. */
private fun placeholderOf(
    state: QuickSwitcherState
): StringResource = when {
    state.purpose == QuickSwitcherPurpose.LINK -> Res.string.switcher_placeholder_link
    state.mode == QuickSwitcherMode.NOTES -> Res.string.switcher_placeholder_notes
    else -> Res.string.switcher_placeholder_content
}

@Composable
private fun SwitcherBody(
    state: QuickSwitcherState,
    onIntent: (QuickSwitcherIntent) -> Unit
) {
    when {
        state.hasUnexpectedError -> Text(
            text = stringResource(resource = Res.string.switcher_error),
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingM),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        state.rows.isNotEmpty() -> Results(
            rows = state.rows,
            selectedIndex = state.selectedIndex,
            onIntent = onIntent
        )

        state.isSearching -> Unit

        else -> NoResults(state = state)
    }
}

@Composable
private fun ModeTabs(
    mode: QuickSwitcherMode,
    isSearching: Boolean,
    onIntent: (QuickSwitcherIntent) -> Unit
) {
    val switchTo = { target: QuickSwitcherMode -> if (target != mode) onIntent(QuickSwitcherIntent.OnModeSwitch) }
    Box {
        NotekeaTabRow(selectedTabIndex = mode.ordinal, modifier = Modifier.fillMaxWidth()) {
            NotekeaTab(
                text = stringResource(resource = Res.string.switcher_mode_notes),
                selected = mode == QuickSwitcherMode.NOTES,
                onClick = { switchTo(QuickSwitcherMode.NOTES) }
            )
            NotekeaTab(
                text = stringResource(resource = Res.string.switcher_mode_content),
                selected = mode == QuickSwitcherMode.CONTENT,
                onClick = { switchTo(QuickSwitcherMode.CONTENT) }
            )
        }
        if (isSearching) {
            NotekeaProgressIndicator(
                label = stringResource(resource = Res.string.switcher_searching),
                modifier = Modifier.align(alignment = Alignment.CenterEnd).padding(end = NotekeaDimensions.SpacingM)
            )
        }
    }
}

@Composable
private fun Results(
    rows: List<QuickSwitcherRow>,
    selectedIndex: Int,
    onIntent: (QuickSwitcherIntent) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, rows) {
        val visible = listState.layoutInfo.visibleItemsInfo
        val isFullyVisible = visible.any { item ->
            item.index == selectedIndex && item.offset >= 0 &&
                item.offset + item.size <= listState.layoutInfo.viewportEndOffset
        }
        if (!isFullyVisible) listState.scrollToItem(index = selectedIndex)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth().heightIn(max = RESULTS_MAX_HEIGHT_DP.dp),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        itemsIndexed(items = rows, key = { _, row ->
            row.path.value
        }, contentType = { _, _ -> ROW_CONTENT_TYPE }) { index, row ->
            NotekeaPaletteItem(
                title = row.name,
                onClick = { onIntent(QuickSwitcherIntent.OnRowClick(path = row.path)) },
                titleHighlights = row.nameRanges,
                detailHighlights = row.detailRanges,
                selected = index == selectedIndex,
                supportingText = row.folder,
                detail = row.detail
            )
        }
    }
}

@Composable
private fun NoResults(
    state: QuickSwitcherState
) {
    val isNotes = state.mode == QuickSwitcherMode.NOTES
    val (title, message) = when {
        state.query.isNotBlank() ->
            Res.string.switcher_no_match_title to
                if (isNotes) Res.string.switcher_no_match_notes else Res.string.switcher_no_match_content

        isNotes -> Res.string.switcher_empty_vault_title to Res.string.switcher_empty_vault_message

        else -> Res.string.switcher_content_hint_title to Res.string.switcher_content_hint_message
    }
    EmptyState(
        title = stringResource(resource = title),
        message = stringResource(resource = message),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuickSwitcherPreviewFrame(
    state: QuickSwitcherState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            QuickSwitcherContent(state = state, onIntent = {})
        }
    }
}

@Preview
@Composable
private fun QuickSwitcherNotesPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherPreviewData.notes())
}

@Preview
@Composable
private fun QuickSwitcherContentPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherPreviewData.content())
}

@Preview
@Composable
private fun QuickSwitcherSearchingPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherPreviewData.content().copy(isSearching = true))
}

@Preview
@Composable
private fun QuickSwitcherNoMatchPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherState(isOpen = true, query = "zzz"))
}

@Preview
@Composable
private fun QuickSwitcherContentHintPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherState(isOpen = true, mode = QuickSwitcherMode.CONTENT))
}

@Preview
@Composable
private fun QuickSwitcherEmptyVaultPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherState(isOpen = true))
}

@Preview
@Composable
private fun QuickSwitcherErrorPreview() {
    QuickSwitcherPreviewFrame(state = QuickSwitcherState(isOpen = true, query = "corn", hasUnexpectedError = true))
}

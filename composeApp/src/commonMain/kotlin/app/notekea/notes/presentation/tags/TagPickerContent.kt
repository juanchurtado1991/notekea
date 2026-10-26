package app.notekea.notes.presentation.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaPalette
import app.notekea.core.presentation.components.NotekeaPaletteItem
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.tag_picker_empty_message
import app.notekea.resources.tag_picker_empty_title
import app.notekea.resources.tag_picker_new
import app.notekea.resources.tag_picker_no_match_message
import app.notekea.resources.tag_picker_no_match_title
import app.notekea.resources.tag_picker_placeholder
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 420
private const val PREVIEW_WIDTH_DP = 760
private const val ROWS_MAX_HEIGHT_DP = 320
private const val TAG_MARK = "#"

/**
 * The tag picker palette (`ROADMAP.md` 1.17) over the workspace, while [TagPickerState.isOpen]: "Find or create a
 * tag", the matching tags as `#tag`, and "New tag: #name" for a name the vault lacks. An empty vault explains that
 * typing a name creates one; text that matches nothing and is not a valid tag says so.
 */
@Composable
fun TagPickerContent(
    state: TagPickerState,
    onIntent: (TagPickerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isOpen) return
    NotekeaPalette(
        query = state.query,
        onQueryChange = { onIntent(TagPickerIntent.OnQueryChange(query = it)) },
        placeholder = stringResource(resource = Res.string.tag_picker_placeholder),
        onMove = { step -> onIntent(if (step > 0) TagPickerIntent.OnSelectNext else TagPickerIntent.OnSelectPrevious) },
        onConfirm = { onIntent(TagPickerIntent.OnConfirm) },
        onDismiss = { onIntent(TagPickerIntent.OnClose) },
        modifier = modifier
    ) {
        if (state.rows.isEmpty()) {
            NoTags(query = state.query)
        } else {
            Rows(rows = state.rows, selectedIndex = state.selectedIndex, onIntent = onIntent)
        }
    }
}

/** The rows, scrolling in a short window and keeping the selected one in view as the arrows move it. */
@Composable
private fun Rows(
    rows: List<TagPickerRow>,
    selectedIndex: Int,
    onIntent: (TagPickerIntent) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex, rows) {
        val isVisible = listState.layoutInfo.visibleItemsInfo.any { item ->
            item.index == selectedIndex && item.offset >= 0 &&
                item.offset + item.size <= listState.layoutInfo.viewportEndOffset
        }
        if (!isVisible) listState.scrollToItem(index = selectedIndex)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth().heightIn(max = ROWS_MAX_HEIGHT_DP.dp),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        itemsIndexed(items = rows, key = { _, row -> row.name to row.isNew }) { index, row ->
            NotekeaPaletteItem(
                title = if (row.isNew) stringResource(Res.string.tag_picker_new, row.name) else TAG_MARK + row.name,
                onClick = { onIntent(TagPickerIntent.OnRowClick(index = index)) },
                selected = index == selectedIndex
            )
        }
    }
}

@Composable
private fun NoTags(
    query: String
) {
    val isSearching = query.isNotBlank()
    EmptyState(
        title = stringResource(
            resource = if (isSearching) Res.string.tag_picker_no_match_title else Res.string.tag_picker_empty_title
        ),
        message = stringResource(
            resource = if (isSearching) Res.string.tag_picker_no_match_message else Res.string.tag_picker_empty_message
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TagPickerPreviewFrame(
    state: TagPickerState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            TagPickerContent(state = state, onIntent = {})
        }
    }
}

@Preview
@Composable
private fun TagPickerContentPreview() {
    TagPickerPreviewFrame(
        state = TagPickerState(
            isOpen = true,
            query = "rec",
            rows = listOf(
                TagPickerRow(name = "recipe"),
                TagPickerRow(name = "recipe/drinks"),
                TagPickerRow(name = "rec", isNew = true)
            )
        )
    )
}

@Preview
@Composable
private fun TagPickerContentEmptyPreview() {
    TagPickerPreviewFrame(state = TagPickerState(isOpen = true))
}

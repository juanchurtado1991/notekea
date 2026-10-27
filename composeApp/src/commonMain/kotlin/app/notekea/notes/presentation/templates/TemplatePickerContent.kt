package app.notekea.notes.presentation.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaPalette
import app.notekea.core.presentation.components.NotekeaPaletteItem
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.tree.notesErrorMessage
import app.notekea.resources.Res
import app.notekea.resources.template_example_name
import app.notekea.resources.template_example_text
import app.notekea.resources.template_picker_create
import app.notekea.resources.template_picker_empty_message
import app.notekea.resources.template_picker_empty_title
import app.notekea.resources.template_picker_new_note_placeholder
import app.notekea.resources.template_picker_no_folder_message
import app.notekea.resources.template_picker_no_folder_title
import app.notekea.resources.template_picker_no_match_message
import app.notekea.resources.template_picker_no_match_title
import app.notekea.resources.template_picker_placeholder
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 420
private const val PREVIEW_WIDTH_DP = 760
private const val ROWS_MAX_HEIGHT_DP = 320

/**
 * The template picker palette (`ROADMAP.md` 1.12) over the workspace, while [TemplatePickerState.isOpen]: "Find a
 * template to insert" (or "Choose a template for the new note") and the matching templates by name. Without a
 * templates folder it explains what one is for and offers Create, which makes it with an example; an empty folder and
 * a search that matches nothing say what to do.
 */
@Composable
fun TemplatePickerContent(
    state: TemplatePickerState,
    platformConventions: PlatformConventions,
    onIntent: (TemplatePickerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isOpen) return
    val placeholder = if (state.target is TemplateTarget.NewNote) {
        Res.string.template_picker_new_note_placeholder
    } else {
        Res.string.template_picker_placeholder
    }
    NotekeaPalette(
        query = state.query,
        onQueryChange = { onIntent(TemplatePickerIntent.OnQueryChange(query = it)) },
        placeholder = stringResource(resource = placeholder),
        onMove = { step ->
            onIntent(if (step > 0) TemplatePickerIntent.OnSelectNext else TemplatePickerIntent.OnSelectPrevious)
        },
        onConfirm = { onIntent(TemplatePickerIntent.OnConfirm) },
        onDismiss = { onIntent(TemplatePickerIntent.OnClose) },
        modifier = modifier
    ) {
        when {
            !state.hasFolder -> NoFolder(state = state, platformConventions = platformConventions, onIntent = onIntent)
            state.rows.isEmpty() -> NoTemplates(state = state)
            else -> Rows(rows = state.rows, selectedIndex = state.selectedIndex, onIntent = onIntent)
        }
    }
}

/** The rows, scrolling in a short window and keeping the selected one in view as the arrows move it. */
@Composable
private fun Rows(
    rows: List<TemplatePickerRow>,
    selectedIndex: Int,
    onIntent: (TemplatePickerIntent) -> Unit
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
        itemsIndexed(items = rows, key = { _, row -> row.path.value }) { index, row ->
            NotekeaPaletteItem(
                title = row.name,
                onClick = { onIntent(TemplatePickerIntent.OnRowClick(index = index)) },
                selected = index == selectedIndex
            )
        }
    }
}

/** No templates folder: what it is for, and Create, which makes it with an example (or why that failed). */
@Composable
private fun NoFolder(
    state: TemplatePickerState,
    platformConventions: PlatformConventions,
    onIntent: (TemplatePickerIntent) -> Unit
) {
    val exampleName = stringResource(resource = Res.string.template_example_name)
    val exampleText = stringResource(resource = Res.string.template_example_text)
    EmptyState(
        title = stringResource(resource = Res.string.template_picker_no_folder_title),
        message = state.error?.let { notesErrorMessage(error = it, platformConventions = platformConventions) }
            ?: stringResource(Res.string.template_picker_no_folder_message, state.folder),
        modifier = Modifier.fillMaxWidth(),
        action = {
            NotekeaButton(
                text = stringResource(resource = Res.string.template_picker_create),
                onClick = {
                    onIntent(
                        TemplatePickerIntent.OnCreateFolderClick(exampleName = exampleName, exampleText = exampleText)
                    )
                },
                enabled = !state.isCreating
            )
        }
    )
}

/** An empty templates folder, or a search that matches none of its templates. */
@Composable
private fun NoTemplates(
    state: TemplatePickerState
) {
    EmptyState(
        title = stringResource(
            resource = if (state.hasTemplates) {
                Res.string.template_picker_no_match_title
            } else {
                Res.string.template_picker_empty_title
            }
        ),
        message = if (state.hasTemplates) {
            stringResource(Res.string.template_picker_no_match_message, state.folder)
        } else {
            stringResource(Res.string.template_picker_empty_message, state.folder)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TemplatePickerPreviewFrame(
    state: TemplatePickerState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            TemplatePickerContent(state = state, platformConventions = PreviewPlatformConventions.linux, onIntent = {})
        }
    }
}

@Preview
@Composable
private fun TemplatePickerContentPreview() {
    TemplatePickerPreviewFrame(
        state = TemplatePickerState(
            isOpen = true,
            rows = listOf("Meeting notes", "Weekly review", "Work/Project kickoff").map {
                TemplatePickerRow(path = RelativePath(value = "Templates/$it.md"), name = it)
            },
            hasTemplates = true
        )
    )
}

@Preview
@Composable
private fun TemplatePickerNoFolderPreview() {
    TemplatePickerPreviewFrame(state = TemplatePickerState(isOpen = true, hasFolder = false))
}

@Preview
@Composable
private fun TemplatePickerEmptyPreview() {
    TemplatePickerPreviewFrame(state = TemplatePickerState(isOpen = true))
}

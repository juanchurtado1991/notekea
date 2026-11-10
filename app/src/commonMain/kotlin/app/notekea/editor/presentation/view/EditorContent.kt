package app.notekea.editor.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.DayNeighbors
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaFindBar
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.PressedModifiers
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.model.EditorHeaderData
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.SaveStatus
import app.notekea.editor.presentation.sidepanel.OutlineTracker
import app.notekea.editor.presentation.viewmodel.ScrollMemory
import app.notekea.resources.Res
import app.notekea.resources.editor_find_count
import app.notekea.resources.editor_find_none
import org.jetbrains.compose.resources.stringResource

internal const val NO_LIGATURES = "liga 0, calt 0"
private const val PREVIEW_HEIGHT_DP = 520
private const val PREVIEW_WIDTH_DP = 760

/**
 * The note editor: the questions that need an answer on top (outside change, deleted note, failed save), then the
 * note's folder, its title, which renames it, and the body, centred at a readable width; the header scrolls with it.
 * Leaving the text saves at once. Files dropped anywhere on the editor are attached at the cursor.
 */
@Composable
fun EditorContent(
    state: EditorState,
    buffer: TextFieldState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    textFocusRequester: FocusRequester? = null,
    outlineTracker: OutlineTracker? = null,
    dayNeighbors: DayNeighbors? = null,
    isBookmarked: Boolean = false,
    scrollMemory: ScrollMemory? = null
) {
    val currentState = rememberUpdatedState(newValue = state)
    val listState = rememberLazyListState()
    val focusRequester = textFocusRequester ?: remember { FocusRequester() }

    val page = remember(buffer, listState, focusRequester) {
        EditorPageState(
            buffer = buffer,
            listState = listState,
            focusRequester = focusRequester,
            isSource = { currentState.value.isSourceMode }
        )
    }

    val pressed = remember { PressedModifiers() }

    val fileDrop = LocalFileDrop.current { paths ->
        onIntent(EditorIntent.OnFilesDrop(paths = paths))
    }

    FollowScroll(
        state = state,
        page = page,
        memory = scrollMemory
    )

    FollowOutline(
        state = state,
        page = page,
        tracker = outlineTracker ?: remember { OutlineTracker() }
    )

    FollowMatch(
        page = page,
        find = state.find
    )

    val surface = modifier
        .fillMaxSize()
        .then(other = fileDrop)
        .then(other = pressed.tracking())

    Surface(
        modifier = surface,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            EditorTop(
                state = state,
                platformConventions = platformConventions,
                onIntent = onIntent
            )

            EditorList(
                state = state,
                page = page,
                header = EditorHeaderData(
                    dayNeighbors = dayNeighbors,
                    isBookmarked = isBookmarked
                ),
                platformConventions = platformConventions,
                onIntent = withModClicks(
                    pressed = pressed,
                    onIntent = onIntent
                )
            )
        }
    }
}

/** What stays above the page while it scrolls: the questions that need an answer, and the find bar. */
@Composable
private fun EditorTop(
    state: EditorState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    EditorBanners(
        state = state,
        platformConventions = platformConventions,
        onIntent = onIntent
    )

    state.find?.let { find ->
        EditorFindBar(
            find = find,
            onIntent = onIntent
        )
    }
}

/**
 * [onIntent], with a link clicked while `Mod` was held ([pressed]) opening behind the current tab:
 * the Markdown renderer only says which link was clicked, not how.
 */
private fun withModClicks(
    pressed: PressedModifiers,
    onIntent: (EditorIntent) -> Unit
): (EditorIntent) -> Unit = { intent ->
    onIntent(
        if (intent is EditorIntent.OnLinkClick) {
            intent.copy(
                inBackground = pressed.isModPressed
            )
        } else {
            intent
        }
    )
}

@Composable
private fun EditorFindBar(
    find: FindState,
    onIntent: (EditorIntent) -> Unit
) {
    val current = find.current

    val label = when {
        find.query.isEmpty() -> ""
        current == null -> stringResource(resource = Res.string.editor_find_none)
        else -> stringResource(Res.string.editor_find_count, current + 1, find.matches.size)
    }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = NotekeaDimensions.SpacingM)) {
        NotekeaFindBar(
            query = find.query,
            onQueryChange = { onIntent(EditorIntent.OnFindQueryChange(query = it)) },
            matchLabel = label,
            onNext = { onIntent(EditorIntent.OnFindNext) },
            onPrevious = { onIntent(EditorIntent.OnFindPrevious) },
            onClose = { onIntent(EditorIntent.OnFindClose) },
            modifier = Modifier.align(alignment = Alignment.CenterEnd)
        )
    }
}

@Composable
private fun EditorPreviewFrame(state: EditorState) {
    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            EditorContent(
                state = state,
                buffer = EditorPreviewData.buffer(),
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}

@Preview
@Composable
private fun EditorContentPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state())
}

@Preview
@Composable
private fun EditorContentUnsavedPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(saveStatus = SaveStatus.UNSAVED))
}

@Preview
@Composable
private fun EditorContentFindPreview() {
    EditorPreviewFrame(
        state = EditorPreviewData.state().copy(
            find = FindState(
                query = "corn",
                matches = listOf(EditorPreviewData.CORN_MATCH),
                current = 0
            )
        )
    )
}

@Preview
@Composable
private fun EditorContentLoadingPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(isLoading = true))
}

@Preview
@Composable
private fun EditorContentConflictPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(hasConflict = true))
}

@Preview
@Composable
private fun EditorContentDeletedPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(isDeleted = true))
}

@Preview
@Composable
private fun EditorContentErrorsPreview() {
    EditorPreviewFrame(
        state = EditorPreviewData.state().copy(
            saveError = NotesError.OperationFailed(path = EditorPreviewData.path.value),
            titleError = NotesError.NameTaken(name = "Pasta e fagioli.md")
        )
    )
}

@Preview
@Composable
private fun EditorContentLoadErrorPreview() {
    EditorPreviewFrame(
        state = EditorPreviewData.state().copy(
            loadError = NotesError.OperationFailed(path = EditorPreviewData.path.value)
        )
    )
}

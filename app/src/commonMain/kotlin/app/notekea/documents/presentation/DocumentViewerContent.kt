package app.notekea.documents.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaProgressIndicator
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.documents.domain.PageMark
import app.notekea.resources.Res
import app.notekea.resources.document_loading
import app.notekea.resources.document_missing
import app.notekea.resources.document_no_text
import app.notekea.resources.document_open_in_app
import app.notekea.resources.document_unreadable
import org.jetbrains.compose.resources.stringResource

private const val PAGE_RATIO = 0.707f
private const val MARK_ALPHA = 0.35f
private const val TEXT_MAX_WIDTH_DP = 760
private const val PAGE_MAX_WIDTH_DP = 900

/**
 * A document's read-only tab: its name and "Open in the system's app" above, then a PDF's real pages (drawn as they
 * come into view) or the document's text; the passage asked for is scrolled to and marked.
 */
@Composable
fun DocumentViewerContent(
    state: DocumentViewerState,
    onIntent: (DocumentViewerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Header(
            name = state.name,
            onOpen = { onIntent(DocumentViewerIntent.OnOpenInApp) }
        )

        Box(
            modifier = Modifier.weight(weight = 1f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.isLoading -> NotekeaProgressIndicator(
                    label = stringResource(resource = Res.string.document_loading),
                    modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl)
                )

                state.isPaged -> Pages(
                    state = state,
                    onPageShown = { onIntent(DocumentViewerIntent.OnPageShown(page = it)) }
                )

                state.error != null -> Problem(error = state.error)

                else -> Blocks(state = state)
            }
        }
    }
}

@Composable
private fun Header(
    name: String,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = NotekeaDimensions.SpacingL,
                vertical = NotekeaDimensions.SpacingS
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )

        NotekeaButton(
            text = stringResource(resource = Res.string.document_open_in_app),
            onClick = onOpen,
            style = NotekeaButtonStyle.SECONDARY
        )
    }
}

@Composable
private fun Pages(
    state: DocumentViewerState,
    onPageShown: (Int) -> Unit
) {
    val list = rememberLazyListState()
    val shown = state.shown

    LaunchedEffect(shown?.count, shown?.page) {
        shown?.page?.let { list.animateScrollToItem(index = it) }
    }

    LazyColumn(
        state = list,
        modifier = Modifier.fillMaxSize().widthIn(max = PAGE_MAX_WIDTH_DP.dp),
        contentPadding = PaddingValues(all = NotekeaDimensions.SpacingL),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
    ) {
        items(
            count = state.pageCount,
            key = { it },
            contentType = { PAGE }
        ) { page ->
            LaunchedEffect(page) { onPageShown(page) }

            val marks = if (shown?.page == page) shown.marks else emptyList()

            Page(
                marks = marks,
                image = state.pages[page]
            )
        }
    }
}

@Composable
private fun Page(
    marks: List<PageMark>,
    image: androidx.compose.ui.graphics.ImageBitmap?
) {
    val ratio = image?.let {
        it.width.toFloat() / it.height
    } ?: PAGE_RATIO

    val markColor = MaterialTheme
        .colorScheme
        .tertiary
        .copy(alpha = MARK_ALPHA)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio = ratio)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        image?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            marks.forEach { mark ->
                drawRect(
                    color = markColor,
                    topLeft = Offset(
                        x = mark.left * size.width,
                        y = mark.top * size.height
                    ),
                    size = Size(
                        width = mark.width * size.width,
                        height = mark.height * size.height
                    )
                )
            }
        }
    }
}

@Composable
private fun Blocks(state: DocumentViewerState) {
    val list = rememberLazyListState()
    val shown = state.shown

    val markColor = MaterialTheme
        .colorScheme
        .tertiary
        .copy(alpha = MARK_ALPHA)

    ScrollToPassage(
        list = list,
        state = state
    )

    LazyColumn(
        state = list,
        modifier = Modifier.fillMaxSize().widthIn(max = TEXT_MAX_WIDTH_DP.dp),
        contentPadding = PaddingValues(all = NotekeaDimensions.SpacingXl),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        itemsIndexed(
            items = state.blocks,
            key = { _, block -> block.range.first },
            contentType = { _, block -> block.level }
        ) { _, block ->
            val isMarked =
                shown != null &&
                    block.range.first <= shown.range.last &&
                    block.range.last >= shown.range.first

            val marking = if (isMarked) Modifier.background(color = markColor) else Modifier

            Text(
                text = block.text,
                modifier = marking,
                style = styleOf(block = block)
            )
        }
    }
}

@Composable
private fun ScrollToPassage(
    list: LazyListState,
    state: DocumentViewerState
) {
    val shown = state.shown

    LaunchedEffect(shown?.count, state.blocks.size) {
        shown ?: return@LaunchedEffect

        val index = state.blocks.indexOfFirst {
            it.range.last >= shown.range.first
        }

        if (index >= 0) list.animateScrollToItem(index = index)
    }
}

@Composable
private fun styleOf(block: DocumentBlock) = when {
    block.level in 1..2 -> MaterialTheme.typography.titleLarge

    block.level >= 3 -> MaterialTheme.typography.titleMedium

    block.isTableRow ->
        MaterialTheme
            .typography
            .bodyMedium
            .copy(fontFamily = FontFamily.Monospace)

    else -> MaterialTheme.typography.bodyLarge
}

@Composable
private fun Problem(error: DocumentError) {
    val message = when (error) {
        DocumentError.Missing -> stringResource(resource = Res.string.document_missing)

        DocumentError.NoText -> stringResource(resource = Res.string.document_no_text)

        is DocumentError.Unreadable ->
            stringResource(resource = Res.string.document_unreadable, error.message)
    }

    Text(
        text = message,
        modifier = Modifier.padding(all = NotekeaDimensions.SpacingXl),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private const val PAGE = "page"

@Preview
@Composable
private fun DocumentViewerContentPreview() {
    NotekeaPreviewThemes {
        DocumentViewerContent(
            state = DocumentViewerState(
                name = "Lisbon guide.docx",
                format = DocumentFormat.WORD,
                isLoading = false,
                blocks = DocumentBlock.of(text = "# Lisbon\n\nTrams climb the hills of Alfama.\n\n## Food\n\nTry the pastéis de nata.\n"),
                shown = ShownPassage(
                    range = 10..40,
                    page = null,
                    marks = emptyList(),
                    count = 1
                )
            ),
            onIntent = {}
        )
    }
}

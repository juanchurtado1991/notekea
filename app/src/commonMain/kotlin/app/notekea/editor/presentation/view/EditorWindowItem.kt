package app.notekea.editor.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.markdown.TextMarks
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.domain.TableCell
import app.notekea.editor.domain.TableText
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The list's item of the cursor's blocks: the text field ([EditorWindow]) with their pictures and query tables under
 * it. With the cursor under its block's text, past a blank line, the block is drawn (with Find's [marks]) and the
 * field is under it; a block that is one table is drawn with its bar, the field over the cell being edited.
 */
@Composable
internal fun EditorWindowItem(
    page: EditorPageState,
    attachments: List<AttachmentView>,
    isSource: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    find: FindState?,
    marks: TextMarks?,
    modifier: Modifier = Modifier
) {
    val table = page.table
    val cell = page.cell
    val underStart = page.underStart
    val place = remember(page) { CellPlace() }

    val frame = if (cell == null) {
        Modifier
    } else {
        Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = MaterialTheme.colorScheme.primary
            )
            .padding(all = NotekeaDimensions.TableCellPadding)
    }

    KeepInCell(page = page)

    Layout(
        content = {
            EditorWindow(
                page = page,
                platformConventions = platformConventions,
                onIntent = onIntent,
                find = find,
                modifier = frame
            )

            Column {
                when {
                    table != null && cell != null -> WindowTable(
                        page = page,
                        table = table,
                        cell = cell,
                        place = place,
                        attachments = attachments,
                        onIntent = onIntent
                    )

                    underStart != null -> WindowBlock(
                        page = page,
                        underStart = underStart,
                        attachments = attachments,
                        onIntent = onIntent,
                        marks = marks
                    )

                    // Source is the note as written: no pictures nor query tables under it.
                    !isSource -> WindowEmbeds(
                        page = page,
                        attachments = attachments,
                        onIntent = onIntent
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onPlaced { place.container = it },
        measurePolicy = windowPolicy(
            isBlockDrawn = page.isBlockDrawn,
            place = place,
            cell = cell
        )
    )
}

/**
 * Where the text field (the first child) and what is drawn with it (the second) go: the field over the [cell] being
 * edited, where [place] says it is, or under the drawn block, or over its pictures when the block is not drawn.
 */
private fun windowPolicy(
    isBlockDrawn: Boolean,
    place: CellPlace,
    cell: TableCell?
) = MeasurePolicy { measurables, constraints ->
    val loose = constraints.copy(
        minWidth = 0,
        minHeight = 0
    )

    val rest = measurables[1].measure(constraints = loose)
    val rect = cell?.let { place.of(cell = it) }

    val overCell = rect?.let {
        Constraints(
            minWidth = it.width.roundToInt(),
            maxWidth = it.width.roundToInt(),
            minHeight = it.height.roundToInt()
        )
    }

    val field = measurables[0].measure(constraints = overCell ?: loose)

    val fieldTop = when {
        cell != null -> rect?.top?.roundToInt() ?: 0
        isBlockDrawn -> rest.height
        else -> 0
    }

    val height = if (cell == null) {
        field.height + rest.height
    } else {
        max(
            a = rest.height,
            b = fieldTop + field.height
        )
    }

    // Until the table says where the cell is, the field is not shown: it would cover the table.
    val fieldAlpha = if (cell != null && rect == null) 0f else 1f

    layout(
        width = constraints.maxWidth,
        height = height
    ) {
        rest.place(
            x = 0,
            y = if (isBlockDrawn) 0 else field.height
        )

        field.placeWithLayer(
            x = rect?.left?.roundToInt() ?: 0,
            y = fieldTop,
            zIndex = 1f
        ) { alpha = fieldAlpha }
    }
}

/**
 * The cursor's block drawn as it reads, with the blank line that ends it, while the cursor is under it: what
 * follows [underStart] is the field's. A click on it puts the cursor back in its text.
 */
@Composable
private fun WindowBlock(
    page: EditorPageState,
    underStart: Int,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit,
    marks: TextMarks?
) {
    val block = page.items.window.first

    val start = page
        .items
        .blocks
        .start(index = block)

    EditorBlock(
        text = page
            .buffer
            .text
            .substring(
                startIndex = start,
                endIndex = underStart - 1
            ),
        textStart = start,
        attachments = attachments,
        onIntent = onIntent,
        onClick = { position, drawnHeight, isExtending ->
            page.click(
                block = block,
                position = position,
                drawnHeight = drawnHeight,
                isExtending = isExtending
            )
        },
        onCellClick = { row, column, x ->
            page.clickCell(
                block = block,
                row = row,
                column = column,
                x = x
            )
        },
        marks = marks
    )
}

/** The table whose [cell] is being edited, drawn: its cells take the cursor, the edited one tells its [place]. */
@Composable
private fun WindowTable(
    page: EditorPageState,
    table: TableText,
    cell: TableCell,
    place: CellPlace,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit
) {
    val block = page.items.window.first

    val text = page.items.textOf(
        text = page.buffer.text,
        block = block
    )

    // Under the cell's field every blank line that ends the block keeps its height.
    val blankLines = text.count {
        it == NEW_LINE_CHAR
    } - text.trimEnd().count {
        it == NEW_LINE_CHAR
    }

    val lineHeight = with(LocalDensity.current) {
        MaterialTheme
            .typography
            .bodyLarge
            .lineHeight
            .toDp()
    }

    TableMenuArea(onIntent = onIntent) {
        EditorTable(
            text = text.trimEnd(),
            textStart = table.start,
            attachments = attachments,
            onIntent = onIntent,
            cells = { row, column ->
                val isEdited = cell.row == row && cell.column == column

                val press = Modifier
                    .cellPress(isMenuKept = true) { x ->
                        page.clickCell(
                            block = block,
                            row = row,
                            column = column,
                            x = x
                        )
                    }

                if (isEdited) {
                    press.then(
                        other = place.watch(
                            row = row,
                            column = column
                        )
                    )
                } else {
                    press
                }
            },
            bar = { TableBar(onIntent = onIntent) }
        )
    }

    Spacer(modifier = Modifier.height(height = lineHeight * blankLines))
}

/**
 * Keeps the cursor in its cell's text: an arrow, an undo or a click on the table's edge can leave it in the pipes or
 * the spaces around it.
 */
@Composable
private fun KeepInCell(page: EditorPageState) {
    LaunchedEffect(page) {
        snapshotFlow { page.strayCursor() }
            .filterNotNull()
            .collect {
                page.buffer.edit { selection = TextRange(index = it) }
            }
    }
}

@Preview
@Composable
private fun EditorWindowItemPreview() {
    val buffer = remember { EditorPreviewData.buffer() }
    val listState = rememberLazyListState()

    val page = remember(buffer, listState) {
        EditorPageState(
            buffer = buffer,
            listState = listState,
            focusRequester = FocusRequester(),
            isSource = { false }
        )
    }

    NotekeaPreviewThemes {
        EditorWindowItem(
            page = page,
            attachments = emptyList(),
            isSource = false,
            platformConventions = EditorPreviewData.platformConventions,
            onIntent = {},
            find = null,
            marks = null
        )
    }
}

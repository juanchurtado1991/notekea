package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.markdown.TableAlignment
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import com.mikepenz.markdown.compose.elements.LocalTableRowIndex
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableBasicText
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private const val PREVIEW_TABLE =
    "| Item | Qty | Note |\n| :-- | --: | :-: |\n| Corn flour | 2 | cups |\n| Salt | 1 | pinch |\n"
private const val PREVIEW_WIDTH_DP = 520

/**
 * What a table's cells get on top of their own look, by row (the header is 0) and column, from who draws the table:
 * the editor takes their clicks with it. None, and the cells are only drawn.
 */
val LocalMarkdownTableCells = compositionLocalOf<((row: Int, column: Int) -> Modifier)?> { null }

/**
 * A table of the Markdown, with each column lined up as its delimiter cell says (`:--`, `:-:`, `--:`) and every cell
 * taking [LocalMarkdownTableCells].
 */
@Composable
internal fun NotekeaMarkdownTable(
    content: String,
    node: ASTNode,
    style: TextStyle
) {
    val alignments = remember(content, node) {
        alignmentsOf(
            content = content,
            table = node
        )
    }

    MarkdownTable(
        content = content,
        node = node,
        style = style,
        headerBlock = { text, row, tableWidth, rowStyle ->
            TableRow(
                content = text,
                row = row,
                index = 0,
                tableWidth = tableWidth,
                style = rowStyle.copy(fontWeight = FontWeight.Bold),
                alignments = alignments
            )
        },
        rowBlock = { text, row, tableWidth, rowStyle ->
            TableRow(
                content = text,
                row = row,
                index = LocalTableRowIndex.current,
                tableWidth = tableWidth,
                style = rowStyle,
                alignments = alignments
            )
        }
    )
}

@Composable
private fun TableRow(
    content: String,
    row: ASTNode,
    index: Int,
    tableWidth: Dp,
    style: TextStyle,
    alignments: List<TableAlignment>
) {
    val cells = LocalMarkdownTableCells.current

    Row(modifier = Modifier.widthIn(min = tableWidth).height(intrinsicSize = IntrinsicSize.Max)) {
        row
            .children
            .filter {
                it.type == GFMTokenTypes.CELL
            }
            .forEachIndexed { column, cell ->
                val place = Modifier
                    .weight(weight = 1f)
                    .fillMaxHeight()
                    .then(other = cells?.invoke(index, column) ?: Modifier)
                    .padding(all = NotekeaDimensions.TableCellPadding)
                    .semantics {
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = index,
                            rowSpan = 1,
                            columnIndex = column,
                            columnSpan = 1
                        )
                    }

                val alignment = when (alignments.getOrNull(index = column)) {
                    TableAlignment.CENTER -> Alignment.Center
                    TableAlignment.RIGHT -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }

                Box(
                    modifier = place,
                    contentAlignment = alignment
                ) {
                    MarkdownTableBasicText(
                        content = content,
                        cell = cell,
                        style = style
                    )
                }
            }
    }
}

/** How each column of [table] lines up: what its delimiter row says, the row between the header and the rest. */
private fun alignmentsOf(
    content: String,
    table: ASTNode
): List<TableAlignment> = table
    .children
    .firstOrNull {
        it.type == GFMTokenTypes.TABLE_SEPARATOR
    }
    ?.getTextInNode(allFileText = content)
    ?.trim()
    ?.removeSurrounding(delimiter = MarkdownSyntax.TABLE_COLUMN)
    ?.split(MarkdownSyntax.TABLE_COLUMN)
    ?.map {
        TableAlignment.of(delimiterCell = it) ?: TableAlignment.NONE
    }
    .orEmpty()

@Preview
@Composable
private fun NotekeaMarkdownTablePreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaMarkdown(content = PREVIEW_TABLE)
        }
    }
}

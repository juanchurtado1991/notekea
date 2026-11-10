package app.notekea.editor.presentation.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.components.NotekeaContextMenuArea
import app.notekea.core.presentation.designsystem.components.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.editor.domain.TableAction
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.toolbar.ToolbarButton
import app.notekea.resources.Res
import app.notekea.resources.table_align_center
import app.notekea.resources.table_align_left
import app.notekea.resources.table_align_right
import app.notekea.resources.table_column_left
import app.notekea.resources.table_column_right
import app.notekea.resources.table_delete_column
import app.notekea.resources.table_delete_row
import app.notekea.resources.table_row_above
import app.notekea.resources.table_row_below
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** What the table's bar and its menu offer, in groups: each action with its icon and its name. */
private val TABLE_ACTIONS: List<List<Triple<TableAction, ImageVector, StringResource>>>
    get() = listOf(
        listOf(
            Triple(TableAction.ROW_ABOVE, NotekeaIcons.RowAbove, Res.string.table_row_above),
            Triple(TableAction.ROW_BELOW, NotekeaIcons.RowBelow, Res.string.table_row_below)
        ),
        listOf(
            Triple(TableAction.COLUMN_LEFT, NotekeaIcons.ColumnLeft, Res.string.table_column_left),
            Triple(
                TableAction.COLUMN_RIGHT,
                NotekeaIcons.ColumnRight,
                Res.string.table_column_right
            )
        ),
        listOf(
            Triple(TableAction.ALIGN_LEFT, NotekeaIcons.AlignLeft, Res.string.table_align_left),
            Triple(
                TableAction.ALIGN_CENTER,
                NotekeaIcons.AlignCenter,
                Res.string.table_align_center
            ),
            Triple(TableAction.ALIGN_RIGHT, NotekeaIcons.AlignRight, Res.string.table_align_right)
        ),
        listOf(
            Triple(TableAction.DELETE_ROW, NotekeaIcons.DeleteRow, Res.string.table_delete_row),
            Triple(
                TableAction.DELETE_COLUMN,
                NotekeaIcons.DeleteColumn,
                Res.string.table_delete_column
            )
        )
    )

/** The bar over the table the cursor is in: rows and columns added from its cell or deleted, its column aligned. */
@Composable
internal fun TableBar(
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TABLE_ACTIONS.forEachIndexed { index, group ->
            if (index > 0) Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))

            group.forEach { (action, icon, label) ->
                ToolbarButton(
                    icon = icon,
                    label = label,
                    onClick = { onIntent(EditorIntent.OnTableEdit(action = action)) }
                )
            }
        }
    }
}

/** The bar's actions on a right click anywhere in the table ([content]); the keyboard stays in the cell. */
@Composable
internal fun TableMenuArea(
    onIntent: (EditorIntent) -> Unit,
    content: @Composable () -> Unit
) {
    NotekeaContextMenuArea(
        menu = {
            TABLE_ACTIONS.mapIndexed { index, group ->
                group.map { (action, icon, label) ->
                    NotekeaMenuItem(
                        label = stringResource(resource = label),
                        onClick = { onIntent(EditorIntent.OnTableEdit(action = action)) },
                        icon = icon,
                        isDestructive = index == TABLE_ACTIONS.lastIndex
                    )
                }
            }
        },
        takesFocus = false,
        content = content
    )
}

@Preview
@Composable
private fun TableBarPreview() {
    NotekeaPreviewThemes {
        TableBar(onIntent = {})
    }
}

@Preview
@Composable
private fun TableMenuAreaPreview() {
    NotekeaPreviewThemes {
        TableMenuArea(onIntent = {}) {
            TableBar(onIntent = {})
        }
    }
}

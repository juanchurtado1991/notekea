package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val CELL_DP = 18
private const val CELL_GAP_DP = 4
private const val PREVIEW_COLUMNS = 8
private const val PREVIEW_ROWS = 6
private const val SELECTED_ALPHA = 0.25f

/**
 * Picks a size on a grid of [columns] × [rows] small cells, as word processors pick a table's size: moving the pointer
 * over the grid fills the cells from the top left corner to the one under it, [label] says the size below the grid, and
 * a click picks it ([onPick] with the columns and rows). It starts on [initialColumns] × [initialRows].
 */
@Composable
fun NotekeaGridSizePicker(
    columns: Int,
    rows: Int,
    label: @Composable (columns: Int, rows: Int) -> String,
    onPick: (columns: Int, rows: Int) -> Unit,
    modifier: Modifier = Modifier,
    initialColumns: Int = 3,
    initialRows: Int = 2
) {
    var size by remember { mutableStateOf(value = initialColumns to initialRows) }
    val step = with(LocalDensity.current) { (CELL_DP + CELL_GAP_DP).dp.toPx() }
    val cellOf = { x: Float, y: Float ->
        ((x / step).toInt() + 1).coerceIn(range = 1..columns) to ((y / step).toInt() + 1).coerceIn(range = 1..rows)
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        Column(
            modifier = Modifier.pointerInput(columns, rows) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.first().position
                        if (event.type != PointerEventType.Exit) size = cellOf(position.x, position.y)
                        if (event.type == PointerEventType.Release) onPick(size.first, size.second)
                    }
                }
            },
            verticalArrangement = Arrangement.spacedBy(space = CELL_GAP_DP.dp)
        ) {
            repeat(times = rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(space = CELL_GAP_DP.dp)) {
                    repeat(times = columns) { column ->
                        GridCell(isSelected = column < size.first && row < size.second)
                    }
                }
            }
        }
        Text(
            text = label(size.first, size.second),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GridCell(
    isSelected: Boolean
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(size = CELL_DP.dp)
            .background(
                color = if (isSelected) colors.primary.copy(alpha = SELECTED_ALPHA) else colors.surface,
                shape = RectangleShape
            )
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = if (isSelected) colors.primary else colors.outline,
                shape = RectangleShape
            )
    )
}

@Preview
@Composable
private fun NotekeaGridSizePickerPreview() {
    NotekeaPreviewThemes {
        NotekeaGridSizePicker(
            columns = PREVIEW_COLUMNS,
            rows = PREVIEW_ROWS,
            label = { columns, rows -> "$columns × $rows table" },
            onPick = { _, _ -> }
        )
    }
}

package app.notekea.core.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_HEIGHT_DP = 120
private const val PREVIEW_ROW = "Row "
private const val PREVIEW_ROWS = 20
private const val PREVIEW_WIDTH_DP = 200

/**
 * A thin scrollbar along the right edge of what [state] scrolls (a `verticalScroll` column), so a list that does not
 * fit says it scrolls: shown only while there is more to see, darker while hovered or dragged. Desktop draws it; the
 * Android previews have none.
 */
@Composable
expect fun NotekeaScrollbar(
    state: ScrollState,
    modifier: Modifier = Modifier
)

/** [NotekeaScrollbar] for a lazy list's [state]. */
@Composable
expect fun NotekeaLazyScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier
)

@Preview
@Composable
private fun NotekeaScrollbarPreview() {
    NotekeaPreviewThemes {
        val state = rememberScrollState()
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            Column(modifier = Modifier.verticalScroll(state = state)) {
                repeat(times = PREVIEW_ROWS) { Text(text = PREVIEW_ROW + it) }
            }
            NotekeaScrollbar(state = state, modifier = Modifier.align(alignment = Alignment.CenterEnd).fillMaxHeight())
        }
    }
}

@Preview
@Composable
private fun NotekeaLazyScrollbarPreview() {
    NotekeaPreviewThemes {
        val state = rememberLazyListState()
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            LazyColumn(state = state) { items(count = PREVIEW_ROWS) { Text(text = PREVIEW_ROW + it) } }
            NotekeaLazyScrollbar(
                state = state,
                modifier = Modifier.align(alignment = Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

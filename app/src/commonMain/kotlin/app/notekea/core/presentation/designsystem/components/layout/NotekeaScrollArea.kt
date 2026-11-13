package app.notekea.core.presentation.designsystem.components.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private const val PREVIEW_HEIGHT_DP = 120
private const val PREVIEW_ROW = "Row "
private const val PREVIEW_ROWS = 20
private const val PREVIEW_WIDTH_DP = 200

/**
 * [content] (one layout), a `verticalScroll` column on [state], with its [NotekeaScrollbar] along the right edge: as
 * tall as the area, shown only while there is more to see.
 */
@Composable
fun NotekeaScrollArea(
    state: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = {
            content()
            NotekeaScrollbar(state = state)
        },
        modifier = modifier,
        measurePolicy = ScrollAreaMeasurePolicy
    )
}

/**
 * [NotekeaScrollArea] for [content], a lazy list on [state]. A lazy list cannot tell how tall it would be, so a parent
 * that asks needs its [contentHeight], given when the rows' heights are known.
 */
@Composable
fun NotekeaLazyScrollArea(
    state: LazyListState,
    modifier: Modifier = Modifier,
    contentHeight: Dp? = null,
    content: @Composable () -> Unit
) {
    Layout(
        content = {
            content()
            NotekeaLazyScrollbar(state = state)
        },
        modifier = modifier,
        measurePolicy = remember(contentHeight) {
            contentHeight?.let { KnownHeightMeasurePolicy(height = it) } ?: ScrollAreaMeasurePolicy
        }
    )
}

@Preview
@Composable
private fun NotekeaScrollAreaPreview() {
    NotekeaPreviewThemes {
        val state = rememberScrollState()

        NotekeaScrollArea(
            state = state,
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            Column(modifier = Modifier.verticalScroll(state = state)) {
                repeat(times = PREVIEW_ROWS) { Text(text = PREVIEW_ROW + it) }
            }
        }
    }
}

@Preview
@Composable
private fun NotekeaLazyScrollAreaPreview() {
    NotekeaPreviewThemes {
        val state = rememberLazyListState()

        NotekeaLazyScrollArea(
            state = state,
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            LazyColumn(state = state) {
                items(count = PREVIEW_ROWS) { Text(text = PREVIEW_ROW + it) }
            }
        }
    }
}

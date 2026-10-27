package app.notekea.core.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val HOVER_ALPHA = 0.6f
private const val HOVER_MILLIS = 150
private const val IDLE_ALPHA = 0.3f
private const val MIN_THUMB_DP = 24
private const val FULL_ROUND = 50
private const val THICKNESS_DP = 6

@Composable
actual fun NotekeaScrollbar(
    state: ScrollState,
    modifier: Modifier
) {
    VerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState = state), modifier = modifier, style = style())
}

@Composable
actual fun NotekeaLazyScrollbar(
    state: LazyListState,
    modifier: Modifier
) {
    VerticalScrollbar(adapter = rememberScrollbarAdapter(scrollState = state), modifier = modifier, style = style())
}

/** A rounded thumb in the muted color, faint until hovered. */
@Composable
private fun style(): ScrollbarStyle {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    return ScrollbarStyle(
        minimalHeight = MIN_THUMB_DP.dp,
        thickness = THICKNESS_DP.dp,
        shape = RoundedCornerShape(percent = FULL_ROUND),
        hoverDurationMillis = HOVER_MILLIS,
        unhoverColor = muted.copy(alpha = IDLE_ALPHA),
        hoverColor = muted.copy(alpha = HOVER_ALPHA)
    )
}

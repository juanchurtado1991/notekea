package app.notekea.core.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The Android target only renders the previews, which need no scrollbar.

@Composable
actual fun NotekeaScrollbar(
    state: ScrollState,
    modifier: Modifier
) = Unit

@Composable
actual fun NotekeaLazyScrollbar(
    state: LazyListState,
    modifier: Modifier
) = Unit

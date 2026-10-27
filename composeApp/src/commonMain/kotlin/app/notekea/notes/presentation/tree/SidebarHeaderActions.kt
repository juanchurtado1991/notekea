package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Immutable
import app.notekea.notes.domain.TreeSort

/** What the sidebar header's buttons do: change the sort, expand or collapse every folder, and minimize the sidebar. */
@Immutable
internal data class SidebarHeaderActions(
    val onSortChange: (TreeSort) -> Unit,
    val onExpandAll: () -> Unit,
    val onCollapseAll: () -> Unit,
    val onMinimize: () -> Unit
)

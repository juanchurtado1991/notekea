package app.notekea.notes.domain

/**
 * How the user left the vault sidebar: which folders were open, how notes are sorted, how wide the sidebar is
 * (between [MIN_SIDEBAR_WIDTH_DP] and [MAX_SIDEBAR_WIDTH_DP]) and whether it is hidden. Folders are per vault; the
 * rest is the same for every vault.
 */
data class TreePreferences(
    val expandedFolders: Set<RelativePath>,
    val sort: TreeSort,
    val sidebarWidthDp: Int = DEFAULT_SIDEBAR_WIDTH_DP,
    val isSidebarCollapsed: Boolean = false
) {
    companion object {
        /** As wide as the outline panel on the other side, so the note sits in the middle (1.12 smoke request). */
        const val DEFAULT_SIDEBAR_WIDTH_DP = SidePanelPreferences.DEFAULT_WIDTH_DP
        const val MAX_SIDEBAR_WIDTH_DP = 480
        const val MIN_SIDEBAR_WIDTH_DP = 200
        val DEFAULT = TreePreferences(expandedFolders = emptySet(), sort = TreeSort.NAME)

        fun clampSidebarWidth(
            widthDp: Int
        ): Int = widthDp.coerceIn(minimumValue = MIN_SIDEBAR_WIDTH_DP, maximumValue = MAX_SIDEBAR_WIDTH_DP)
    }
}

package app.notekea.notes.domain

/** How the user left the tree of a vault: which folders were open and how notes are sorted. */
data class TreePreferences(
    val expandedFolders: Set<RelativePath>,
    val sort: TreeSort
) {
    companion object {
        val DEFAULT = TreePreferences(expandedFolders = emptySet(), sort = TreeSort.NAME)
    }
}

package app.notekea.search.presentation.switcher

/**
 * State of the quick switcher palette. [rows] are the results for [query] in [mode], and [selectedIndex] the one
 * `Enter` opens. [isSearching] is true while a content search runs. [purpose] says whether the chosen note is opened or
 * linked from the open note. [showsExactWords] tells that content search only has the exact words.
 */
data class QuickSwitcherState(
    val isOpen: Boolean = false,
    val mode: QuickSwitcherMode = QuickSwitcherMode.NOTES,
    val query: String = "",
    val rows: List<QuickSwitcherRow> = emptyList(),
    val selectedIndex: Int = 0,
    val isSearching: Boolean = false,
    val hasUnexpectedError: Boolean = false,
    val purpose: QuickSwitcherPurpose = QuickSwitcherPurpose.OPEN,
    val isIndexReady: Boolean = true,
    val isMeaningOff: Boolean = false
) {
    val showsExactWords: Boolean
        get() =
            mode == QuickSwitcherMode.CONTENT &&
                query.isNotBlank() &&
                (!isIndexReady || isMeaningOff)
}

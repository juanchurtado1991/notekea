package app.notekea.notes.presentation.switcher

/**
 * State of the quick switcher palette (`ROADMAP.md` 1.6). [rows] are the results for [query] in [mode], at most 10,
 * and [selectedIndex] the one `Enter` opens. [isSearching] is true while a content search runs (the name search is
 * instant). With nothing found the palette says "No notes match"; an empty content search shows a hint instead.
 */
data class QuickSwitcherState(
    val isOpen: Boolean = false,
    val mode: QuickSwitcherMode = QuickSwitcherMode.NOTES,
    val query: String = "",
    val rows: List<QuickSwitcherRow> = emptyList(),
    val selectedIndex: Int = 0,
    val isSearching: Boolean = false,
    val hasUnexpectedError: Boolean = false
)

package app.notekea.notes.presentation.tags

/**
 * State of the tag picker (`ROADMAP.md` 1.17): the vault's tags matching [query] as [rows], the most used first, and
 * the typed text as a new tag last when the vault lacks it; [selectedIndex] is the one `Enter` adds.
 */
data class TagPickerState(
    val isOpen: Boolean = false,
    val query: String = "",
    val rows: List<TagPickerRow> = emptyList(),
    val selectedIndex: Int = 0
)

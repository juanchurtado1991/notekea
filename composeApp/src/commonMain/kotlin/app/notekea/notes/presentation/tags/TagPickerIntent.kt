package app.notekea.notes.presentation.tags

/** What the user did in the tag picker, plus the vault's tags as they change. */
sealed interface TagPickerIntent {
    /** The toolbar's Tag button, with the selected text as the first search. */
    data class OnOpen(val query: String = "") : TagPickerIntent

    data object OnClose : TagPickerIntent

    /** `Enter`: adds the selected tag. */
    data object OnConfirm : TagPickerIntent

    data class OnQueryChange(val query: String) : TagPickerIntent

    data class OnRowClick(val index: Int) : TagPickerIntent

    data object OnSelectNext : TagPickerIntent

    data object OnSelectPrevious : TagPickerIntent

    /** The vault's tags, most used first, sent whenever the tree changes. */
    data class OnTagsChange(val tags: List<String>) : TagPickerIntent
}

package app.notekea.notes.presentation.editor

/**
 * Find in note while its bar is open: the [query], where it appears in the body ([matches], in order) and which one is
 * the current match ([current], an index into [matches], or null when there is none).
 */
data class FindState(
    val query: String = "",
    val matches: List<IntRange> = emptyList(),
    val current: Int? = null
)

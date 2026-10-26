package app.notekea.notes.domain

/**
 * The tags of a vault, in the frontmatter or written in the text, ordered for picking one (the toolbar's tag picker,
 * `ROADMAP.md` 1.17): the most used first (a note counts once per tag), ties in alphabetical order ignoring case.
 */
class TagRanking {
    fun byUse(
        notes: Collection<NoteEntry>
    ): List<String> = notes.flatMap { (it.metadata.tags + it.metadata.textTags).distinct() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase() })
        .map { it.key }
}

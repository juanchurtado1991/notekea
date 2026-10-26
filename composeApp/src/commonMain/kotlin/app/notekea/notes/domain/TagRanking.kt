package app.notekea.notes.domain

/**
 * The frontmatter tags of a vault ordered for picking one (the toolbar's Tag menu, `ROADMAP.md` 1.17): the most used
 * first, ties in alphabetical order ignoring letter case.
 */
class TagRanking {
    fun byUse(
        notes: Collection<NoteEntry>
    ): List<String> = notes.flatMap { it.metadata.tags }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase() })
        .map { it.key }
}

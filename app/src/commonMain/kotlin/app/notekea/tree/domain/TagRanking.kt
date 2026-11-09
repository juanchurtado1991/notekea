package app.notekea.tree.domain

import app.notekea.core.notes.domain.NoteEntry

/**
 * The tags of a vault, in the frontmatter or written in the text, ordered for picking one in the toolbar's tag
 * picker: the most used first (a note counts once per tag), ties in alphabetical order ignoring case.
 */
class TagRanking {
    fun byUse(notes: Collection<NoteEntry>): List<String> = notes
        .flatMap { (it.metadata.tags + it.metadata.textTags).distinct() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase() }
        )
        .map { it.key }
}

package app.notekea.notes.domain

/**
 * Finds where [names] are written as plain text in Markdown (`ROADMAP.md` 2.5, unlinked mentions): whole words in any
 * letter case, never inside code, a link, a web address or a `#tag`. Ranges are in order and never overlap.
 */
interface MentionFinder {
    fun find(
        markdown: String,
        names: List<String>
    ): List<IntRange>
}

package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * Finds tags for the tag picker (`ROADMAP.md` 1.17) among the vault's `tags`, already ranked by [TagRanking]: typed
 * text (a leading `#` ignored, letter case too) matches tags that start with it first, then tags that contain it,
 * each group in rank order. [newTag] is the typed text as a tag to create, when it is a valid tag the vault lacks.
 */
class TagSearch {
    fun search(
        tags: List<String>,
        query: String,
        limit: Int
    ): List<String> {
        val typed = query.trim().removePrefix(prefix = MarkdownSyntax.TAG_MARK).lowercase()
        if (typed.isEmpty()) return tags.take(n = limit)
        val (starting, others) = tags.filter { it.lowercase().contains(other = typed) }
            .partition { it.lowercase().startsWith(prefix = typed) }
        return (starting + others).take(n = limit)
    }

    /** [query] as a new tag (spaces become `-`), or null when it is empty, not a valid tag or already in `tags`. */
    fun newTag(
        tags: List<String>,
        query: String
    ): String? {
        val name = query.trim().removePrefix(prefix = MarkdownSyntax.TAG_MARK)
            .replace(regex = StringPatterns.WHITESPACE, replacement = StringPatterns.HYPHEN)
        val isValid = MarkdownSyntax.TAG_NAME.matches(input = name)
        return name.takeIf { isValid && tags.none { it.equals(other = name, ignoreCase = true) } }
    }
}

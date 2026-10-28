package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns.SPACE

/**
 * The `#tags` written in a note's text, as Obsidian reads them, each once in the order they first appear: a `#` not
 * after a letter, digit, `#`, `/` or `&`, followed by letters, digits, `_`, `-` and `/`, not only digits. Code (fenced
 * blocks and inline code), wiki links and link targets are not text, so `[[#Heading]]` and `(#anchor)` are not tags.
 */
class TextTagFinder {
    fun tags(
        lines: Sequence<String>
    ): List<String> {
        val tags = LinkedHashSet<String>()
        var fence: Char? = null
        lines.forEach { line ->
            // Most lines have no fence and no tag: the cheap character checks skip their regexes.
            val marker = if (line.any { it in FENCE_CHARS }) {
                MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)?.first()
            } else {
                null
            }
            when {
                fence != null -> if (marker == fence) fence = null

                marker != null -> fence = marker

                TAG_MARK in line -> {
                    val text = line.replace(regex = NOT_TEXT, replacement = SPACE)
                    MarkdownSyntax.TAG.findAll(input = text).forEach { tags += it.groupValues[1] }
                }
            }
        }
        return tags.toList()
    }

    companion object {
        private const val FENCE_CHARS = "`~"
        private const val TAG_MARK = '#'
        private val NOT_TEXT = Regex(pattern = "(`+).*?\\1|\\[\\[[^\\]\\n]*]]|]\\([^)\\n]*\\)")
    }
}

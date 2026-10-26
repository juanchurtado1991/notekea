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
            val marker = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)?.first()
            when {
                fence != null -> if (marker == fence) fence = null

                marker != null -> fence = marker

                else -> {
                    val text = line.replace(regex = NOT_TEXT, replacement = SPACE)
                    MarkdownSyntax.TAG.findAll(input = text).forEach { tags += it.groupValues[1] }
                }
            }
        }
        return tags.toList()
    }

    companion object {
        private val NOT_TEXT = Regex(pattern = "(`+).*?\\1|\\[\\[[^\\]\\n]*]]|]\\([^)\\n]*\\)")
    }
}

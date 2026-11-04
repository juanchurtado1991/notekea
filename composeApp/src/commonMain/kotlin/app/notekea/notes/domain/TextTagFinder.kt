package app.notekea.notes.domain

import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns

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
        val fences = FenceTracker()
        lines.forEach { line ->
            if (!fences.read(text = line)) tagsIn(line = line).forEach { tags += it.groupValues[1] }
        }
        return tags.toList()
    }

    /**
     * The `#tags` of a [line] outside fenced code, each match at its place in the line, its name in group 1. Most lines
     * have no tag: the cheap character check skips the regexes.
     */
    fun tagsIn(
        line: String
    ): Sequence<MatchResult> = if (TAG_MARK in line) {
        MarkdownSyntax.TAG.findAll(input = textOf(line = line))
    } else {
        emptySequence()
    }

    /** [line] with what is not text blanked, every other character where it was. */
    private fun textOf(
        line: String
    ): String = NOT_TEXT.replace(input = line) { StringPatterns.SPACE.repeat(n = it.value.length) }

    companion object {
        private const val TAG_MARK = '#'
        private val NOT_TEXT = Regex(pattern = "(`+).*?\\1|\\[\\[[^\\]\\n]*]]|]\\([^)\\n]*\\)")
    }
}

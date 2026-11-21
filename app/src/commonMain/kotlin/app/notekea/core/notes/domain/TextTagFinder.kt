package app.notekea.core.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The `#tags` written in a note's text, as Obsidian reads them, each once in the order they first appear (the rule is
 * `MarkdownSyntax.TAG`). Code (fenced blocks and inline code), wiki links, link targets and addresses are not text,
 * so `[[#Heading]]`, `(#anchor)` and `https://a.b/#top` are not tags.
 */
class TextTagFinder {
    fun tags(lines: Sequence<String>): List<String> {
        val tags = LinkedHashSet<String>()
        val fences = FenceTracker()

        lines.forEach { line ->
            if (!fences.read(text = line)) {
                tagsIn(line = line).forEach { tags += it.groupValues[1] }
            }
        }

        return tags.toList()
    }

    /**
     * The `#tags` of a [line] outside fenced code, each match at its place in the line, its name in group 1. Most lines
     * have no tag: the cheap character check skips the regexes.
     */
    fun tagsIn(line: String): Sequence<MatchResult> = if (TAG_MARK in line) {
        MarkdownSyntax.TAG.findAll(input = textOf(line = line))
    } else {
        emptySequence()
    }

    /** [line] with what is not text (code, wiki links, link targets) blanked, every other character where it was. */
    fun textOf(line: String): String = NOT_TEXT
        .replace(input = line) { StringPatterns.SPACE.repeat(n = it.value.length) }

    companion object {
        private const val TAG_MARK = '#'

        // A link's target may hold one level of parentheses (`wiki/Dune_(novel)#Plot`) and a bare address its
        // `#anchor`: either ended the target early, and the anchor after it made a tag.
        private val NOT_TEXT = Regex(
            pattern = "(`+).*?\\1|\\[\\[[^\\]\\n]*]]|]\\((?:[^()\\n]|\\([^()\\n]*\\))*\\)|https?://\\S+"
        )
    }
}

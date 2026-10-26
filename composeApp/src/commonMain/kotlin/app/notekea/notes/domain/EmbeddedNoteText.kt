package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The part of a note an embed shows, as Obsidian does (`ROADMAP.md` 1.8): the whole note without its frontmatter for
 * `![[Note]]`, or one section for `![[Note#Heading]]`, from that heading down to the next heading of the same level or
 * higher. Null for a heading the note does not have and for a block (`#^id`), which then stay links.
 */
class EmbeddedNoteText {
    fun of(
        text: String,
        target: String
    ): String? {
        val body = FRONTMATTER.replaceFirst(input = text, replacement = StringPatterns.EMPTY)
        val anchor = target.substringAfter(
            delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR,
            missingDelimiterValue = StringPatterns.EMPTY
        )
        if (anchor.isEmpty()) return body.trim()
        if (anchor.startsWith(char = MarkdownSyntax.BLOCK_ANCHOR_CHAR)) return null
        return section(body = body, heading = anchor.trim())
    }

    private fun section(
        body: String,
        heading: String
    ): String? {
        val lines = body.split(StringPatterns.NEW_LINE_CHAR)
        val start = lines.indexOfFirst { headingOf(line = it)?.second.equals(other = heading, ignoreCase = true) }
        val level = lines.getOrNull(index = start)?.let { headingOf(line = it)?.first } ?: return null
        val end = (start + 1 until lines.size).firstOrNull { index ->
            headingOf(line = lines[index])?.let { it.first <= level } == true
        } ?: lines.size
        return lines.subList(fromIndex = start, toIndex = end).joinToString(separator = StringPatterns.NEW_LINE).trim()
    }

    /** The level and text of a heading line (`## Dough` → 2 and "Dough"), null for any other line. */
    private fun headingOf(
        line: String
    ): Pair<Int, String>? = HEADING.matchEntire(input = line)?.let {
        it.groupValues[1].length to
            it.groupValues[2].trim()
    }

    companion object {
        private val FRONTMATTER = Regex(pattern = "\\A---\\r?\\n[\\s\\S]*?\\r?\\n---\\r?\\n?")
        private val HEADING = Regex(pattern = "^(#{1,6})\\s+(.*?)\\s*#*\\s*$")
    }
}

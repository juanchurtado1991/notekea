package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The part of a note an embed shows, as Obsidian does (`ROADMAP.md` 1.8, 1.15): the whole note without its frontmatter
 * for `![[Note]]`, one section for `![[Note#Heading]]`, from that heading down to the next heading of the same level
 * or higher, or one block for `![[Note#^id]]`: the list item or paragraph whose last line ends with ` ^id`, or the
 * block just above a line holding only `^id` (a table, a quote), shown without its marker. Null for a heading or block
 * the note does not have.
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
        if (anchor.startsWith(char = MarkdownSyntax.BLOCK_ANCHOR_CHAR)) {
            return block(
                body = body,
                id = anchor.removePrefix(prefix = MarkdownSyntax.BLOCK_ANCHOR_CHAR.toString()).trim()
            )
        }
        return section(body = body, heading = anchor.trim())
    }

    /** The block marked `^id`, without its marker; null when no line carries it. */
    private fun block(
        body: String,
        id: String
    ): String? {
        val marker = blockMarker(id = id)
        val lines = body.split(StringPatterns.NEW_LINE_CHAR).map { it.trimEnd() }
        val marked = lines.indexOfFirst { marker.containsMatchIn(input = it) }
        if (id.isEmpty() || marked < 0) return null
        val isOwnLine = lines[marked].trim() == MarkdownSyntax.BLOCK_ANCHOR_CHAR + id
        val end = if (isOwnLine) marked - 1 else marked
        val start = when {
            isOwnLine -> (end downTo 0).firstOrNull { lines[it].isBlank() }?.plus(1) ?: 0
            LIST_ITEM.containsMatchIn(input = lines[marked]) -> marked
            else -> (marked downTo 0).firstOrNull { lines[it].isBlank() }?.plus(1) ?: 0
        }
        if (end < start) return null
        return lines.subList(fromIndex = start, toIndex = end + 1)
            .joinToString(separator = StringPatterns.NEW_LINE)
            .replace(regex = marker, replacement = StringPatterns.EMPTY)
            .trim()
    }

    /** ` ^id` at the end of a line, or `^id` alone on it. */
    private fun blockMarker(
        id: String
    ): Regex = Regex(pattern = "(^|\\s)\\^" + Regex.escape(literal = id) + "\\s*$")

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
        private val LIST_ITEM = Regex(pattern = "^\\s*([-*+]|\\d+[.)])\\s")
    }
}

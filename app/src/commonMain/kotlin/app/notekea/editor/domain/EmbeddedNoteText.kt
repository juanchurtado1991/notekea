package app.notekea.editor.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.OutlineHeading

/**
 * The part of a note an embed shows, as Obsidian does (`ROADMAP.md` 1.8, 1.15): the whole note without its frontmatter
 * for `![[Note]]`, one section for `![[Note#Heading]]`, from that heading down to the next heading of the same level
 * or higher (`![[Note#Parent#Child]]` walks to it through its parents, and headings compare without their letter case,
 * punctuation and extra spaces, so `#Foo Bar` finds `## Foo: Bar`), or one block for `![[Note#^id]]`: the list item or paragraph whose last line ends with ` ^id`, or the
 * block just above a line holding only `^id` (a table, a quote), shown without its marker. Null for a heading or block
 * the note does not have.
 */
class EmbeddedNoteText {
    private val outlineFinder = OutlineFinder()

    fun of(
        text: String,
        target: String
    ): String? {
        val body = NoteDocument.fromText(text = text).body
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
        return section(body = body, path = anchor.split(MarkdownSyntax.HEADING_ANCHOR_CHAR))
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
        path: List<String>
    ): String? {
        val headings = outlineFinder.headings(text = body)
        val parts = path.map { normalized(heading = it) }.filter { it.isNotEmpty() }
        val index = indexOf(headings = headings, path = parts) ?: return null
        val start = headings[index]
        val end = headings.drop(n = index + 1).firstOrNull { it.level <= start.level }?.offset ?: body.length
        return body.substring(startIndex = start.offset, endIndex = end).trim()
    }

    /** Where [path] leads, each part a heading under the one before; leaving a parent's section starts over. */
    private fun indexOf(
        headings: List<OutlineHeading>,
        path: List<String>
    ): Int? {
        if (path.isEmpty()) return null
        var part = 0
        var level = 0
        headings.forEachIndexed { index, heading ->
            if (heading.level <= level) {
                part = 0
                level = 0
            }
            if (normalized(heading = heading.text) == path[part]) {
                part++
                level = heading.level
                if (part == path.size) return index
            }
        }
        return null
    }

    /** [heading] as links compare it: lowercase, with punctuation and runs of spaces as one space. */
    private fun normalized(
        heading: String
    ): String = NOT_IN_ANCHOR.replace(input = heading, replacement = StringPatterns.SPACE).trim().lowercase()

    companion object {
        private val NOT_IN_ANCHOR = Regex(pattern = "[!\"#\$%&()*+,.:;<=>?@^`{|}~/\\[\\]\\\\\\s]+")
        private val LIST_ITEM = Regex(pattern = "^\\s*([-*+]|\\d+[.)])\\s")
    }
}

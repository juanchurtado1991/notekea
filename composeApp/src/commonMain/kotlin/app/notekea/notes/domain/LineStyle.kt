package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns
/**
 * A way to start a line in Markdown, which a toolbar button adds or removes: a heading of a level, a bulleted or
 * numbered list item, a checklist item or a quote. A list item style replaces another list marker on the line.
 */
sealed interface LineStyle {
    /** Whether [line] already starts this way. */
    fun matches(
        line: String
    ): Boolean

    /** [line] starting this way, as item [number] of the lines changed together. */
    fun add(
        line: String,
        number: Int
    ): String

    /** [line] without this start. */
    fun remove(
        line: String
    ): String

    data class Heading(val level: Int) : LineStyle {
        override fun matches(
            line: String
        ): Boolean = HEADING.find(input = line)?.groupValues?.get(index = 1)?.length == level

        override fun add(
            line: String,
            number: Int
        ): String = MarkdownSyntax.HEADING_MARK.repeat(n = level) + StringPatterns.SPACE + remove(line = line)

        override fun remove(
            line: String
        ): String = line.replaceFirst(regex = HEADING, replacement = StringPatterns.EMPTY)
    }

    data object Bullet : LineStyle {
        override fun matches(
            line: String
        ): Boolean = BULLET.containsMatchIn(input = line) && !CHECKLIST.containsMatchIn(input = line)

        override fun add(
            line: String,
            number: Int
        ): String = listItem(line = line, marker = MarkdownSyntax.BULLET)

        override fun remove(
            line: String
        ): String = withoutListMarker(line = line)
    }

    data object Numbered : LineStyle {
        override fun matches(
            line: String
        ): Boolean = NUMBERED.containsMatchIn(input = line)

        override fun add(
            line: String,
            number: Int
        ): String = listItem(
            line = line,
            marker =
            number.toString() + MarkdownSyntax.NUMBER_DELIMITER + StringPatterns.SPACE
        )

        override fun remove(
            line: String
        ): String = withoutListMarker(line = line)
    }

    data object Checklist : LineStyle {
        override fun matches(
            line: String
        ): Boolean = CHECKLIST.containsMatchIn(input = line)

        override fun add(
            line: String,
            number: Int
        ): String = listItem(line = line, marker = MarkdownSyntax.BULLET + MarkdownSyntax.TASK_BOX)

        override fun remove(
            line: String
        ): String = withoutListMarker(line = line)
    }

    data object Quote : LineStyle {
        override fun matches(
            line: String
        ): Boolean = line.startsWith(char = MarkdownSyntax.QUOTE_MARK)

        override fun add(
            line: String,
            number: Int
        ): String = MarkdownSyntax.QUOTE + line

        override fun remove(
            line: String
        ): String = line.replaceFirst(regex = QUOTE, replacement = StringPatterns.EMPTY)
    }

    companion object {
        private val BULLET = Regex(pattern = "^\\s*[-*+] ")
        private val CHECKLIST = Regex(pattern = "^\\s*[-*+] \\[[ xX]] ")
        private val HEADING = Regex(pattern = "^(#{1,6}) ")
        private val LIST_MARKER = Regex(pattern = "^(\\s*)(?:[-*+] \\[[ xX]] |[-*+] |\\d+[.)] )?")
        private val NUMBERED = Regex(pattern = "^\\s*\\d+[.)] ")
        private val QUOTE = Regex(pattern = "^> ?")

        private fun listItem(
            line: String,
            marker: String
        ): String {
            val indent = LIST_MARKER.find(input = line)?.groupValues?.get(index = 1).orEmpty()
            return indent + marker + withoutListMarker(line = line).trimStart()
        }

        private fun withoutListMarker(
            line: String
        ): String {
            val match = LIST_MARKER.find(input = line) ?: return line
            return match.groupValues[1] + line.substring(startIndex = match.range.last + 1)
        }
    }
}

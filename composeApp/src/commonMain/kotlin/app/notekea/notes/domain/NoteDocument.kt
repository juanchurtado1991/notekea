package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR

/**
 * A note split for editing: the [header] (byte order mark and frontmatter block, exactly as stored) that the editor
 * never touches, and the [body] the user edits. The body always uses `\n` line breaks; [usesCrLf] remembers that the
 * file used `\r\n` throughout, so [toText] writes them back. A body that mixes line breaks is kept as it is, so
 * opening and saving a note without edits never changes a byte (`ROADMAP.md` 1.5: frontmatter preserved untouched).
 */
data class NoteDocument(
    val header: String,
    val body: String,
    val usesCrLf: Boolean
) {
    /** The note's full text, as it must be written to disk. */
    fun toText(): String = header + if (usesCrLf) body.replace(oldValue = LF, newValue = CRLF) else body

    companion object {
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val CR = '\r'
        private const val CRLF = "\r\n"
        private const val DELIMITER = "---"
        private const val LF = "\n"

        /**
         * Splits a note's [text]. Frontmatter is recognized as Obsidian does: the first line is exactly `---` and a
         * later line is exactly `---`; anything else (no closing line, `----`, a `---` further down) is body.
         */
        fun fromText(
            text: String
        ): NoteDocument {
            val start = if (text.startsWith(prefix = BYTE_ORDER_MARK)) BYTE_ORDER_MARK.length else 0
            val headerEnd = frontmatterEnd(text = text, start = start) ?: start
            val body = text.substring(startIndex = headerEnd)
            val usesCrLf =
                body.contains(other = CRLF) && body.count { it == NEW_LINE_CHAR } == body.split(CRLF).size - 1
            return NoteDocument(
                header = text.substring(startIndex = 0, endIndex = headerEnd),
                body = if (usesCrLf) body.replace(oldValue = CRLF, newValue = LF) else body,
                usesCrLf = usesCrLf
            )
        }

        /** Where the frontmatter block that starts at [start] ends (after its closing line's break), or null. */
        private fun frontmatterEnd(
            text: String,
            start: Int
        ): Int? {
            var lineStart = start
            var lineIndex = 0
            while (lineStart < text.length) {
                val newline = text.indexOf(char = NEW_LINE_CHAR, startIndex = lineStart)
                val lineEnd = if (newline < 0) text.length else newline
                val line = text.substring(startIndex = lineStart, endIndex = lineEnd).trimEnd(CR)
                val isDelimiter = line == DELIMITER
                if (lineIndex == 0 && !isDelimiter) return null
                if (lineIndex > 0 && isDelimiter) return if (newline < 0) text.length else newline + 1
                if (newline < 0) return null
                lineStart = newline + 1
                lineIndex++
            }
            return null
        }
    }
}

package app.notekea.core.notes.domain

import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR

/**
 * A note split for editing: the [header] (byte order mark and frontmatter, exactly as stored), which the editor never
 * touches, and the [body] the user edits, always with `\n` line breaks. [usesCrLf] remembers that the file used `\r\n`
 * throughout, so [toText] writes them back; a body that mixes line breaks is kept as it is.
 */
data class NoteDocument(
    val header: String,
    val body: String,
    val usesCrLf: Boolean
) {
    /**
     * The note's full text, as it must be written to disk. A header whose closing `---` ended the file gets the file's
     * line break before a body that was typed after it; otherwise the body would join that line and the frontmatter
     * would stop being one.
     */
    fun toText(): String {
        val text = if (usesCrLf) {
            body.replace(
                oldValue = LF,
                newValue = CRLF
            )
        } else {
            body
        }

        return header + breakBeforeBody() + text
    }

    private fun breakBeforeBody(): String = when {
        body.isEmpty() ||
            header.removePrefix(prefix = BYTE_ORDER_MARK).isEmpty() ||
            header.endsWith(suffix = LF) -> ""

        header.endsWith(char = CR) -> LF

        usesCrLf || header.contains(other = CRLF) -> CRLF

        else -> LF
    }

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
        fun fromText(text: String): NoteDocument {
            val start = if (text.startsWith(prefix = BYTE_ORDER_MARK)) BYTE_ORDER_MARK.length else 0

            val headerEnd = frontmatterEnd(
                text = text,
                start = start
            ) ?: start

            val body = text.substring(startIndex = headerEnd)

            val usesCrLf = body.contains(other = CRLF) &&
                body.count { it == NEW_LINE_CHAR } == countOf(text = body, part = CRLF)

            val bodyWithLf = if (usesCrLf) {
                body.replace(
                    oldValue = CRLF,
                    newValue = LF
                )
            } else {
                body
            }

            return NoteDocument(
                header = text.substring(
                    startIndex = 0,
                    endIndex = headerEnd
                ),
                body = bodyWithLf,
                usesCrLf = usesCrLf
            )
        }

        // How many times the part occurs in the text, counted without splitting it.
        private fun countOf(
            text: String,
            part: String
        ): Int {
            var count = 0
            var index = text.indexOf(string = part)

            while (index >= 0) {
                count++

                index = text.indexOf(
                    string = part,
                    startIndex = index + part.length
                )
            }

            return count
        }

        /** Where the frontmatter block that starts at [start] ends (after its closing line's break), or null. */
        private fun frontmatterEnd(
            text: String,
            start: Int
        ): Int? {
            var lineStart = start
            var lineIndex = 0

            while (lineStart < text.length) {
                val newline = text.indexOf(
                    char = NEW_LINE_CHAR,
                    startIndex = lineStart
                )

                val lineEnd = if (newline < 0) text.length else newline

                val line = text.substring(
                    startIndex = lineStart,
                    endIndex = lineEnd
                ).trimEnd(CR)

                val isDelimiter = line == DELIMITER
                if (lineIndex == 0 && !isDelimiter) return null

                if (lineIndex > 0 && isDelimiter) {
                    return if (newline < 0) text.length else newline + 1
                }

                if (newline < 0) return null
                lineStart = newline + 1
                lineIndex++
            }

            return null
        }
    }
}

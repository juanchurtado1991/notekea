package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.RelativePath
import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.percentDecoded

/**
 * Links in Read mode. The renderer knows Markdown links but not Obsidian's `[[wiki links]]` or `#tags`, so [prepare]
 * rewrites each wiki link (outside code) as a Markdown link to `notekea:<target>`, shown with its alias or as written,
 * and each `#tag` (outside code, links and web addresses) as a link to `notekea-tag:<tag>` that searches it
 * (`ROADMAP.md` 1.6); an embed (`![[…]]`) becomes a plain link until images render (`ROADMAP.md` 1.8). [classify]
 * turns a clicked link back into a note to open, a tag to search or an external address.
 */
class ReadModeLinks {
    fun prepare(
        body: String
    ): String {
        var fence: Char? = null
        return body.split(NEW_LINE_CHAR).joinToString(separator = NEW_LINE) { line ->
            val opening = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)
            when {
                fence != null -> line.also {
                    if (line.trim().let { it.length >= MarkdownSyntax.FENCE.length && it.all { c -> c == fence } }) {
                        fence = null
                    }
                }

                opening != null -> line.also { fence = opening.first() }

                else -> linkLine(line = line)
            }
        }
    }

    fun classify(
        uri: String
    ): ReadLink? = when {
        uri.startsWith(
            prefix = TAG_SCHEME
        ) -> ReadLink.Tag(name = percentDecoded(target = uri.removePrefix(prefix = TAG_SCHEME)))

        uri.startsWith(prefix = FILE_SCHEME) -> ReadLink.File(
            target = percentDecoded(target = uri.removePrefix(prefix = FILE_SCHEME))
        )

        uri.startsWith(prefix = SCHEME) -> ReadLink.Note(
            target = percentDecoded(target = uri.removePrefix(prefix = SCHEME)),
            kind = LinkKind.WIKI
        )

        uri.startsWith(char = MarkdownSyntax.HEADING_ANCHOR_CHAR) -> null

        OTHER_SCHEME.containsMatchIn(input = uri) -> ReadLink.External(uri = uri)

        else -> ReadLink.Note(
            target = uri.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR),
            kind = LinkKind.MARKDOWN_ENCODED
        )
    }

    /** Rewrites the tags and wiki links of one line that are not inside inline code. */
    private fun linkLine(
        line: String
    ): String {
        val withTags = tagLine(line = line)
        val code = INLINE_CODE.findAll(input = withTags).map { it.range }.toList()
        return WIKI_LINK.replace(input = withTags) { match ->
            val inCode = code.any { it.first <= match.range.first && match.range.last <= it.last }
            when {
                inCode -> match.value

                match.value.startsWith(prefix = EMBED_MARK) && isFile(inner = match.groupValues[1]) -> fileLink(
                    inner = match.groupValues[1]
                )

                else -> markdownLink(inner = match.groupValues[1]) ?: match.value
            }
        }
    }

    /** Rewrites each `#tag` of one line as a link, leaving alone those inside code, links and web addresses. */
    private fun tagLine(
        line: String
    ): String {
        val protected = listOf(INLINE_CODE, WIKI_LINK, MARKDOWN_LINK, AUTOLINK, WEB_ADDRESS).flatMap { pattern ->
            pattern.findAll(input = line).map { it.range }
        }
        return MarkdownSyntax.TAG.replace(input = line) { match ->
            val isProtected = protected.any { it.first <= match.range.first && match.range.last <= it.last }
            if (isProtected) {
                match.value
            } else {
                link(text = match.value, address = TAG_SCHEME + encoded(text = match.groupValues[1]))
            }
        }
    }

    /** `Arepas#Dough|the arepas` → `[the arepas](notekea:Arepas)`; null for a link to a heading of this note. */
    private fun markdownLink(
        inner: String
    ): String? {
        val written = inner.substringBefore(delimiter = MarkdownSyntax.LINK_LABEL_SEPARATOR_CHAR)
        val label = inner.substringAfter(
            delimiter = MarkdownSyntax.LINK_LABEL_SEPARATOR_CHAR,
            missingDelimiterValue = written
        )
            .trim()
        val target = written.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR)
            .substringBefore(delimiter = MarkdownSyntax.BLOCK_ANCHOR_CHAR)
            .trim()
        if (target.isEmpty()) return null
        val escaped = label.replace(oldValue = MarkdownSyntax.LINK_TEXT_CLOSE, newValue = ESCAPED_LINK_TEXT_CLOSE)
        return link(text = escaped, address = SCHEME + encoded(text = target))
    }

    /** `menu.pdf|The menu` → `[The menu](notekea-file:menu.pdf)`: a file embedded in a paragraph opens when clicked. */
    private fun fileLink(
        inner: String
    ): String {
        val target = inner.substringBefore(delimiter = MarkdownSyntax.LINK_LABEL_SEPARATOR_CHAR).trim()
        val option = inner.substringAfter(
            delimiter = MarkdownSyntax.LINK_LABEL_SEPARATOR_CHAR,
            missingDelimiterValue = target
        )
        val label = option.takeUnless { WIDTH.matches(input = it.trim()) }?.trim() ?: target
        return link(text = label, address = FILE_SCHEME + encoded(text = target))
    }

    /** Whether an embed's [inner] text names a file, not a note: its name has an extension other than `.md`. */
    private fun isFile(
        inner: String
    ): Boolean {
        val name = inner.substringBefore(delimiter = MarkdownSyntax.LINK_LABEL_SEPARATOR_CHAR)
            .substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR)
            .substringAfterLast(delimiter = RelativePath.SEPARATOR)
        return FILE_EXTENSION.containsMatchIn(input = name) &&
            !name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION, ignoreCase = true)
    }

    /** A Markdown link `[text](address)`. */
    private fun link(
        text: String,
        address: String
    ): String =
        MarkdownSyntax.LINK_TEXT_OPEN + text + MarkdownSyntax.LINK_TEXT_CLOSE + MarkdownSyntax.LINK_TARGET_OPEN +
            address + MarkdownSyntax.LINK_TARGET_CLOSE

    /** Percent-encodes everything but unreserved characters and `/`, so the renderer keeps the address whole. */
    private fun encoded(
        text: String
    ): String = text.encodeToByteArray().joinToString(separator = StringPatterns.EMPTY) { byte ->
        val char = (byte.toInt() and BYTE_MASK).toChar()
        if (byte >= 0 && (char.isLetterOrDigit() || char in UNRESERVED)) {
            char.toString()
        } else {
            PERCENT + (byte.toInt() and BYTE_MASK).toString(radix = HEX_RADIX).uppercase()
                .padStart(length = HEX_DIGITS, padChar = StringPatterns.ZERO_CHAR)
        }
    }

    companion object {
        /** The address scheme of a link to a note, as rewritten by [prepare]. */
        const val SCHEME = "notekea:"

        /** The address scheme of a link to a file embedded in a paragraph, as rewritten by [prepare]. */
        const val FILE_SCHEME = "notekea-file:"

        /** The address scheme of a link to search a tag, as rewritten by [prepare]. */
        const val TAG_SCHEME = "notekea-tag:"
        private const val BYTE_MASK = 0xFF
        private const val EMBED_MARK = "!"
        private val FILE_EXTENSION = Regex(pattern = "\\.[A-Za-z0-9]{1,8}$")
        private val WIDTH = Regex(pattern = "\\d{1,5}(?:x\\d{1,5})?")
        private const val ESCAPED_LINK_TEXT_CLOSE = "\\]"
        private const val HEX_DIGITS = 2
        private const val PERCENT = "%"
        private const val HEX_RADIX = 16
        private const val UNRESERVED = "-._~/"
        private val INLINE_CODE = Regex(pattern = "(`+).+?\\1")
        private val OTHER_SCHEME = Regex(pattern = "^[a-zA-Z][a-zA-Z0-9+.-]+:")
        private val WIKI_LINK = Regex(pattern = "!?\\[\\[([^\\]\\n]+)]]")
        private val MARKDOWN_LINK = Regex(pattern = "!?\\[[^\\]\\n]*]\\([^)\\n]*\\)")
        private val AUTOLINK = Regex(pattern = "<[^>\\s]+>")
        private val WEB_ADDRESS = Regex(pattern = "[a-zA-Z][a-zA-Z0-9+.-]*://\\S+")
    }
}

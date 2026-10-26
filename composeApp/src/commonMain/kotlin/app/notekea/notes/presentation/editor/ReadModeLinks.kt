package app.notekea.notes.presentation.editor

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
        return body.split('\n').joinToString(separator = "\n") { line ->
            val opening = FENCE.find(input = line)?.groupValues?.get(index = 1)
            when {
                fence != null -> line.also {
                    if (line.trim().let { it.length >= 3 && it.all { c -> c == fence } }) {
                        fence =
                            null
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

        uri.startsWith(prefix = SCHEME) -> ReadLink.Note(
            target = percentDecoded(target = uri.removePrefix(prefix = SCHEME)),
            kind = LinkKind.WIKI
        )

        uri.startsWith(prefix = "#") -> null

        OTHER_SCHEME.containsMatchIn(input = uri) -> ReadLink.External(uri = uri)

        else -> ReadLink.Note(target = uri.substringBefore(delimiter = '#'), kind = LinkKind.MARKDOWN_ENCODED)
    }

    /** Rewrites the tags and wiki links of one line that are not inside inline code. */
    private fun linkLine(
        line: String
    ): String {
        val withTags = tagLine(line = line)
        val code = INLINE_CODE.findAll(input = withTags).map { it.range }.toList()
        return WIKI_LINK.replace(input = withTags) { match ->
            val inCode = code.any { it.first <= match.range.first && match.range.last <= it.last }
            if (inCode) match.value else markdownLink(inner = match.groupValues[1]) ?: match.value
        }
    }

    /** Rewrites each `#tag` of one line as a link, leaving alone those inside code, links and web addresses. */
    private fun tagLine(
        line: String
    ): String {
        val protected = listOf(INLINE_CODE, WIKI_LINK, MARKDOWN_LINK, AUTOLINK, WEB_ADDRESS).flatMap { pattern ->
            pattern.findAll(input = line).map { it.range }
        }
        return TAG.replace(input = line) { match ->
            val isProtected = protected.any { it.first <= match.range.first && match.range.last <= it.last }
            if (isProtected) {
                match.value
            } else {
                "[" + match.value + "](" + TAG_SCHEME +
                    encoded(text = match.groupValues[1]) +
                    ")"
            }
        }
    }

    /** `Arepas#Dough|the arepas` → `[the arepas](notekea:Arepas)`; null for a link to a heading of this note. */
    private fun markdownLink(
        inner: String
    ): String? {
        val written = inner.substringBefore(delimiter = '|')
        val label = inner.substringAfter(delimiter = '|', missingDelimiterValue = written).trim()
        val target = written.substringBefore(delimiter = '#').substringBefore(delimiter = '^').trim()
        if (target.isEmpty()) return null
        return "[" + label.replace(oldValue = "]", newValue = "\\]") + "](" + SCHEME + encoded(text = target) + ")"
    }

    /** Percent-encodes everything but unreserved characters and `/`, so the renderer keeps the address whole. */
    private fun encoded(
        text: String
    ): String = text.encodeToByteArray().joinToString(separator = "") { byte ->
        val char = (byte.toInt() and BYTE_MASK).toChar()
        if (byte >= 0 && (char.isLetterOrDigit() || char in UNRESERVED)) {
            char.toString()
        } else {
            "%" +
                (byte.toInt() and BYTE_MASK).toString(radix = HEX_RADIX).uppercase().padStart(length = 2, padChar = '0')
        }
    }

    companion object {
        /** The address scheme of a link to a note, as rewritten by [prepare]. */
        const val SCHEME = "notekea:"

        /** The address scheme of a link to search a tag, as rewritten by [prepare]. */
        const val TAG_SCHEME = "notekea-tag:"
        private const val BYTE_MASK = 0xFF
        private const val HEX_RADIX = 16
        private const val UNRESERVED = "-._~/"
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val INLINE_CODE = Regex(pattern = "(`+).+?\\1")
        private val OTHER_SCHEME = Regex(pattern = "^[a-zA-Z][a-zA-Z0-9+.-]+:")
        private val WIKI_LINK = Regex(pattern = "!?\\[\\[([^\\]\\n]+)]]")
        private val MARKDOWN_LINK = Regex(pattern = "!?\\[[^\\]\\n]*]\\([^)\\n]*\\)")
        private val AUTOLINK = Regex(pattern = "<[^>\\s]+>")
        private val WEB_ADDRESS = Regex(pattern = "[a-zA-Z][a-zA-Z0-9+.-]*://\\S+")

        /** A tag as Obsidian reads it: `#` not after a letter, `#`, `/` or `&`, and not only digits. */
        private val TAG = Regex(pattern = "(?<![\\w#/&])#([\\p{L}\\p{N}_/-]*[\\p{L}_/-][\\p{L}\\p{N}_/-]*)")
    }
}

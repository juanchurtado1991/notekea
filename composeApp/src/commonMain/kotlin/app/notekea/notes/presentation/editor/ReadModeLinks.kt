package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.percentDecoded

/**
 * Links in Read mode. The renderer knows Markdown links but not Obsidian's `[[wiki links]]`, so [prepare] rewrites
 * each one (outside code) as a Markdown link to `notekea:<target>`, shown with its alias or as written; an embed
 * (`![[…]]`) becomes a plain link until images render (`ROADMAP.md` 1.8). [classify] turns a clicked link back into a
 * note to open or an external address.
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
        uri.startsWith(prefix = SCHEME) -> ReadLink.Note(
            target = percentDecoded(target = uri.removePrefix(prefix = SCHEME)),
            kind = LinkKind.WIKI
        )

        uri.startsWith(prefix = "#") -> null

        OTHER_SCHEME.containsMatchIn(input = uri) -> ReadLink.External(uri = uri)

        else -> ReadLink.Note(target = uri.substringBefore(delimiter = '#'), kind = LinkKind.MARKDOWN_ENCODED)
    }

    /** Rewrites the wiki links of one line that are not inside inline code. */
    private fun linkLine(
        line: String
    ): String {
        val code = INLINE_CODE.findAll(input = line).map { it.range }.toList()
        return WIKI_LINK.replace(input = line) { match ->
            val inCode = code.any { it.first <= match.range.first && match.range.last <= it.last }
            if (inCode) match.value else markdownLink(inner = match.groupValues[1]) ?: match.value
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
        private const val BYTE_MASK = 0xFF
        private const val HEX_RADIX = 16
        private const val UNRESERVED = "-._~/"
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val INLINE_CODE = Regex(pattern = "(`+).+?\\1")
        private val OTHER_SCHEME = Regex(pattern = "^[a-zA-Z][a-zA-Z0-9+.-]+:")
        private val WIKI_LINK = Regex(pattern = "!?\\[\\[([^\\]\\n]+)]]")
    }
}

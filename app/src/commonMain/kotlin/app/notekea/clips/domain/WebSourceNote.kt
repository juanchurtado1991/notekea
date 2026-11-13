package app.notekea.clips.domain

/**
 * A clipped note read back (the developer's request of 2026-11-13, updating the web sources): the page it came from,
 * how deep its section went, the article kept under "Original text", and the note with a newer article in its place.
 */
object WebSourceNote {
    private val SOURCE = Regex(
        pattern = "^source:\\s*\"?(https?://[^\"\\s]+)\"?\\s*$",
        option = RegexOption.MULTILINE
    )
    private val LEVELS = Regex(
        pattern = "^levels:\\s*(\\d)\\s*$",
        option = RegexOption.MULTILINE
    )
    private val CLIPPED = Regex(
        pattern = "^clipped:.*$",
        option = RegexOption.MULTILINE
    )
    private const val FRONTMATTER = "---"
    private const val LINK = "- [["

    /** The address in the note's `source` property; null for a note that was not clipped. */
    fun sourceOf(text: String): String? = SOURCE
        .find(input = frontmatterOf(text = text))
        ?.groupValues
        ?.get(index = 1)
        ?.replace(
            oldValue = "%22",
            newValue = "\""
        )

    /** The levels of links its section was clipped to, 1 when the note does not say. */
    fun levelsOf(text: String): Int = LEVELS
        .find(input = frontmatterOf(text = text))
        ?.groupValues
        ?.get(index = 1)
        ?.toIntOrNull()
        ?: 1

    /** The article under "Original text", as it was clipped. */
    fun originalOf(text: String): String = text
        .substringAfter(
            delimiter = WebClipNote.ORIGINAL,
            missingDelimiterValue = ""
        )
        .trim()

    /**
     * [text] with [article] as its original text and [today] as the day it was clipped: what the person or the model
     * wrote above it stays as it is.
     */
    fun updated(
        text: String,
        article: String,
        today: String
    ): String {
        val head = text
            .substringBefore(delimiter = WebClipNote.ORIGINAL)
            .replaceFirst(
                regex = CLIPPED,
                replacement = WebClipNote.CLIPPED + today
            )

        return head + WebClipNote.ORIGINAL + "\n\n" + article.trim() + "\n"
    }

    /** The index [text] with [links] ("- [[Folder/Page|Page]]") added at the end of its list of pages. */
    fun withPages(
        text: String,
        links: List<String>
    ): String {
        if (links.isEmpty()) return text
        val lines = text.lines().toMutableList()
        val last = lines.indexOfLast { it.startsWith(prefix = LINK) }
        val added = links.map { "$LINK$it]]" }

        if (last >= 0) {
            lines.addAll(last + 1, added)

            return lines.joinToString(separator = "\n")
        }

        // An index clipped alone has no list yet: it gets one above its original text.
        val list = WebClipSection.PAGES + "\n\n" + added.joinToString(separator = "\n") + "\n\n"

        return text.replaceFirst(
            oldValue = WebClipNote.ORIGINAL,
            newValue = list + WebClipNote.ORIGINAL
        )
    }

    private fun frontmatterOf(text: String): String {
        if (!text.startsWith(prefix = FRONTMATTER)) return ""

        return text
            .removePrefix(prefix = FRONTMATTER)
            .substringBefore(delimiter = "\n$FRONTMATTER")
    }
}

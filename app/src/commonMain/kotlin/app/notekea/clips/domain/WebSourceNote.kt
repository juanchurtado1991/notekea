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

    // The heading alone on its line: "### Original text" or "## Original text of the law" are the person's own.
    private val ORIGINAL = Regex(
        pattern = "^${WebClipNote.ORIGINAL}[ \\t]*\\r?$",
        option = RegexOption.MULTILINE
    )

    private val PAGES = Regex(
        pattern = "^${WebClipSection.PAGES}[ \\t]*\\r?$",
        option = RegexOption.MULTILINE
    )

    private val COPY = Regex(
        pattern = "\\.(md|txt|html?)$",
        option = RegexOption.IGNORE_CASE
    )

    private const val FRONTMATTER = "---"
    private const val LINK = "- [["
    private const val CRLF = "\r\n"
    private const val LF = "\n"

    // A page that lost two thirds of a real article is more likely a wall or a "not found" than a new version.
    private const val GONE_SHARE = 3
    private const val REAL_ARTICLE = 400

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

    /** The article under "Original text", as it was clipped, its lines ending as an article's do. */
    fun originalOf(text: String): String {
        val heading = ORIGINAL.find(input = text) ?: return ""

        return text
            .substring(startIndex = heading.range.last + 1)
            .replace(
                oldValue = CRLF,
                newValue = LF
            )
            .trim()
    }

    /** Whether [article], read again, says what the note at [text] already keeps. */
    fun isSame(
        text: String,
        article: String
    ): Boolean = originalOf(text = text) == article.trim()

    /**
     * Whether [article] looks like the page went away (a cookie wall, a "not found" that answers as a page): far
     * shorter than the real article the note at [text] keeps, which is then left as it is.
     */
    fun looksGone(
        text: String,
        article: String
    ): Boolean {
        val kept = originalOf(text = text).length

        return kept >= REAL_ARTICLE && article.trim().length * GONE_SHARE < kept
    }

    /** Whether [text] is a section's index: it lists its pages above its original text. */
    fun hasPages(text: String): Boolean = PAGES.containsMatchIn(input = headOf(text = text))

    /**
     * [text] with [article] as its original text and [today] as the day it was clipped: what the person or the model
     * wrote above it stays as it is, and the note keeps its own line endings.
     */
    fun updated(
        text: String,
        article: String,
        today: String
    ): String {
        val ending = if (CRLF in text) CRLF else LF

        val head = headOf(text = text).replaceFirst(
            regex = CLIPPED,
            replacement = WebClipNote.CLIPPED + today
        )

        val body = article
            .trim()
            .replace(
                oldValue = LF,
                newValue = ending
            )

        return head + WebClipNote.ORIGINAL + ending + ending + body + ending
    }

    /**
     * The index [text] with [links] ("Folder/Page|Page") added at the end of its list of pages, which is above its
     * original text: a list inside the article is the page's own and never touched.
     */
    fun withPages(
        text: String,
        links: List<String>
    ): String {
        if (links.isEmpty()) return text
        val head = headOf(text = text)
        val rest = text.removePrefix(prefix = head)
        val ending = if (CRLF in text) CRLF else LF
        val added = links.map { "$LINK$it]]" }
        val lines = head.split(ending).toMutableList()

        val pages = lines.indexOfFirst {
            it.trim() == WebClipSection.PAGES
        }

        if (pages < 0) {
            // An index clipped alone has no list yet: it gets one above its original text.
            val list =
                (listOf(WebClipSection.PAGES, "") + added + "").joinToString(separator = ending)

            return head + list + ending + rest
        }

        val last = lines.indexOfLast { it.startsWith(prefix = LINK) }
        lines.addAll(if (last > pages) last + 1 else pages + 1, added)

        return lines.joinToString(separator = ending) + rest
    }

    /** The names of the pages the index [text] lists, as its links call them ("- [[Docs/Rows|Rows]]" is "Rows"). */
    fun pagesOf(text: String): List<String> = headOf(text = text)
        .lines()
        .filter { it.startsWith(prefix = LINK) }
        .map {
            it
                .substringAfter(delimiter = LINK)
                .substringBefore(delimiter = "]]")
                .substringAfter(delimiter = '|')
                .substringAfterLast(delimiter = '/')
                .trim()
        }

    /** An address however it is written: its last slash, "www.", "/index", a ".md" or ".html" copy, upper case. */
    fun keyOf(url: String): String = url
        .substringBefore(delimiter = '#')
        .substringBefore(delimiter = '?')
        .substringAfter(delimiter = "://")
        .removePrefix(prefix = "www.")
        .replace(
            oldValue = "/_llms/",
            newValue = "/"
        )
        .trimEnd('/')
        .replace(
            regex = COPY,
            replacement = ""
        )
        .removeSuffix(suffix = "/index")
        .lowercase()

    // What is above the "Original text" heading; the whole note when it has none.
    private fun headOf(text: String): String {
        val heading = ORIGINAL.find(input = text) ?: return text

        return text.substring(
            startIndex = 0,
            endIndex = heading.range.first
        )
    }

    private fun frontmatterOf(text: String): String {
        if (!text.startsWith(prefix = FRONTMATTER)) return ""

        return text
            .removePrefix(prefix = FRONTMATTER)
            .substringBefore(delimiter = "\n$FRONTMATTER")
    }
}

package app.notekea.clips.data

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/** The text of a paragraph, an item or a cell as Markdown: its bold, italic, code and links, on one line. */
internal object HtmlInline {
    fun of(node: Node): String = tidy(text = written(node = node))

    /** [text] on one line, its runs of spaces one space, no space before a point or a comma. */
    fun tidy(text: String): String = text
        .replace(SPACES, replacement = " ")
        .replace(SPACE_BEFORE_MARK, replacement = "$1")
        .trim()

    private fun written(node: Node): String {
        if (node is TextNode) return node.text()
        val element = node as? Element ?: return ""
        if (HtmlMarkdown.isLeftOut(node = element)) return ""

        val inner = element
            .childNodes()
            .joinToString(separator = "") { written(node = it) }

        val text = inner.trim()
        val tag = element.normalName()

        return when {
            tag == "br" -> " "

            text.isEmpty() -> inner

            // A footnote's number, "[12]": it points into a page the note does not have.
            tag == "sup" && FOOTNOTE.matches(input = text) -> ""

            tag == "strong" || tag == "b" -> spaced(
                inner = inner,
                marked = "**$text**"
            )

            tag == "em" || tag == "i" -> spaced(
                inner = inner,
                marked = "*$text*"
            )

            tag == "code" ||
                tag == "kbd" ||
                tag == "samp" ->
                spaced(
                    inner = inner,
                    marked = "`${element.text().trim()}`"
                )

            tag == "a" -> spaced(
                inner = inner,
                marked = linkOf(
                    link = element,
                    text = text
                )
            )

            else -> inner
        }
    }

    // A link to another page keeps its address, whole; one into this page, or of no address, only its words.
    private fun linkOf(
        link: Element,
        text: String
    ): String {
        val address = link.absUrl("href")
        val isPage = address.startsWith(prefix = "http") && '#' !in link.attr("href").take(n = 1)

        val isPlain =
            !isPage ||
                text.startsWith(prefix = "`") ||
                '[' in text ||
                ']' in text

        if (isPlain) return text

        val safe = address
            .replace(
                oldValue = "(",
                newValue = "%28"
            )
            .replace(
                oldValue = ")",
                newValue = "%29"
            )
            .replace(
                oldValue = " ",
                newValue = "%20"
            )

        return "[$text]($safe)"
    }

    // The spaces the element had around its words stay outside its marks: "a <b>b </b>c" is "a **b** c".
    private fun spaced(
        inner: String,
        marked: String
    ): String {
        val before = if (inner.firstOrNull()?.isWhitespace() == true) " " else ""
        val after = if (inner.lastOrNull()?.isWhitespace() == true) " " else ""

        return before + marked + after
    }

    private val SPACES = Regex(pattern = "\\s+")
    private val SPACE_BEFORE_MARK = Regex(pattern = " ([.,;:!?)])")
    private val FOOTNOTE = Regex(pattern = "\\[?[\\w ]{1,12}]?")
}

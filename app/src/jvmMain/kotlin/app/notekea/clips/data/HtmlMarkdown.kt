package app.notekea.clips.data

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * A page's article as Markdown blocks (the developer's request of 2026-11-13, clips that read well): headings under
 * the note's own, lists with their items and levels, code kept whole in a fence, tables, quotes, and bold, italic,
 * code and links inside a text. What a page draws around its article (menus, forms, hidden parts) is left out.
 */
internal object HtmlMarkdown {
    fun blocksOf(root: Element): List<String> {
        val blocks = mutableListOf<String>()

        collect(
            parent = root,
            blocks = blocks
        )

        return blocks
    }

    // The blocks under [parent], in order: a run of text and inline elements between two blocks is a paragraph.
    private fun collect(
        parent: Element,
        blocks: MutableList<String>
    ) {
        val run = StringBuilder()

        parent.childNodes().forEach { node ->
            val element = node as? Element

            when {
                node is TextNode -> run.append(node.text())

                element == null || isLeftOut(element = element) -> Unit

                isInline(element = element) -> run.append(HtmlInline.of(node = element))

                else -> {
                    flush(
                        run = run,
                        blocks = blocks
                    )

                    block(
                        element = element,
                        blocks = blocks
                    )
                }
            }
        }

        flush(
            run = run,
            blocks = blocks
        )
    }

    private fun block(
        element: Element,
        blocks: MutableList<String>
    ) {
        val tag = element.normalName()

        val made = when {
            tag in HEADINGS -> headingOf(element = element)
            tag == "p" || element.attr("data-as") == "p" -> HtmlInline.of(node = element)
            tag == "ul" || tag == "ol" -> listOf(list = element)
            tag == "pre" -> codeOf(pre = element)
            tag == "blockquote" -> quoteOf(quote = element)
            tag == "table" && element.select("table").size == 1 -> HtmlTable.of(table = element)
            tag == "hr" || tag == "br" -> ""
            else -> null
        }

        when {
            made == null -> collect(
                parent = element,
                blocks = blocks
            )

            made.isNotBlank() -> blocks += made
        }
    }

    private fun flush(
        run: StringBuilder,
        blocks: MutableList<String>
    ) {
        val text = HtmlInline.tidy(text = run.toString())
        if (text.isNotEmpty()) blocks += text
        run.clear()
    }

    // An article's own headings come under the note's "Original text", itself a second-level heading.
    private fun headingOf(element: Element): String {
        val text = element
            .text()
            .trim()
            .trimEnd('¶', '#')
            .trim()

        if (text.isEmpty()) return ""

        val marks = when (element.normalName()) {
            "h1", "h2" -> "###"
            "h3" -> "####"
            else -> "#####"
        }

        return "$marks $text"
    }

    // Each item with its mark; what an item holds after its first block (a nested list, code) goes under it.
    private fun listOf(list: Element): String {
        val isNumbered = list.normalName() == "ol"

        val items = list
            .children()
            .filter {
                it.normalName() == "li"
            }
            .mapIndexedNotNull { index, item ->
                val inner = blocksOf(root = item)
                val mark = if (isNumbered) "${index + 1}. " else "- "
                val under = " ".repeat(n = mark.length)

                inner
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(separator = "\n")
                    ?.lines()
                    ?.mapIndexed { line, text ->
                        if (line == 0) mark + text else under + text
                    }
                    ?.joinToString(separator = "\n")
            }

        return items.joinToString(separator = "\n")
    }

    private fun codeOf(pre: Element): String {
        val whole = pre.wholeText()
        // Lines drawn as elements of their own, with no line break between them (Tailwind's docs).
        val isOneLine = '\n' !in whole.trim() && pre.select("*").size > 2
        val code = (if (isOneLine) linesOf(pre = pre) else whole).trim('\n', '\r')
        if (code.isBlank()) return ""

        val language = LANGUAGE
            .find(input = pre.className() + " " + pre.select("code").attr("class"))
            ?.groupValues
            ?.get(index = 1)
            .orEmpty()

        val fence = if (FENCE in code) LONG_FENCE else FENCE

        return "$fence$language\n$code\n$fence"
    }

    // A line an element each, by its class or, where a reader took the classes away, by being all the code holds.
    private fun linesOf(pre: Element): String {
        val holder = pre.selectFirst("code") ?: pre
        val parts = holder.childNodes()
        val isAllLines = parts.size >= 2 && parts.all { it is Element }

        if (isAllLines) return parts.joinToString(separator = "\n") { (it as Element).wholeText() }

        return marked(node = pre)
    }

    private fun marked(node: Node): String = when {
        node is TextNode -> node.wholeText

        node is Element && node.normalName() == "br" -> "\n"

        node is Element -> {
            val inner = node.childNodes().joinToString(separator = "") { marked(node = it) }
            val isLine = node.isBlock || LINE.containsMatchIn(input = node.className())
            if (isLine && node.normalName() != "pre") inner + "\n" else inner
        }

        else -> ""
    }

    private fun quoteOf(quote: Element): String = blocksOf(root = quote)
        .joinToString(separator = "\n>\n") { block ->
            block.lines().joinToString(separator = "\n") { "> $it" }
        }

    private fun isInline(element: Element): Boolean {
        val isParagraph = element.attr("data-as") == "p"
        val holdsBlocks = element.select(BLOCKS_INSIDE).isNotEmpty()

        return !element.isBlock &&
            !isParagraph &&
            !holdsBlocks &&
            element.normalName() != "br"
    }

    private fun isLeftOut(element: Element): Boolean {
        val isHidden =
            element.hasAttr("hidden") ||
                element.attr("aria-hidden") == "true" ||
                HIDDEN.containsMatchIn(input = element.attr("style"))

        return isHidden || element.normalName() in LEFT_OUT
    }

    /** Whether [node] is something a page draws around or inside its text that a note does not keep. */
    fun isLeftOut(node: Node): Boolean = node is Element && isLeftOut(element = node)

    private const val FENCE = "```"
    private val LINE = Regex(pattern = "\\bline\\b")
    private const val LONG_FENCE = "~~~~"
    private const val BLOCKS_INSIDE =
        "p, div, ul, ol, pre, table, blockquote, h1, h2, h3, h4, h5, h6, section"
    private val HEADINGS = setOf("h1", "h2", "h3", "h4", "h5", "h6")
    private val LANGUAGE = Regex(pattern = "(?:language|lang|highlight-source)-([\\w+#-]+)")
    private val HIDDEN = Regex(pattern = "display\\s*:\\s*none|visibility\\s*:\\s*hidden")

    private val LEFT_OUT = setOf(
        "script", "style", "noscript", "template", "svg", "canvas", "nav", "aside", "footer", "form", "button",
        "input", "select", "textarea", "iframe", "dialog", "video", "audio", "img", "picture", "object", "embed"
    )
}

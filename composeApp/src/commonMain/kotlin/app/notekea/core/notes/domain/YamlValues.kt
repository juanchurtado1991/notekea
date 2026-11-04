package app.notekea.core.notes.domain

/** How frontmatter values are written and read back: quoting, list items and the words YAML gives a type. */
internal object YamlValues {
    const val COMMENT_MARK = "#"
    const val FALSE = "false"
    const val INLINE_CLOSE = "]"
    const val INLINE_OPEN = "["
    const val ITEM_MARK = "-"
    const val TRUE = "true"
    private const val APOSTROPHE = '\''
    private const val ESCAPE = '\\'
    private const val ITEM_SEPARATOR = ','
    private const val KEY_END = ':'
    private const val QUOTE = '"'
    private const val QUOTES = "'\""
    private const val SPECIAL_STARTS = "-?:,[]{}#&*!|>'\"%@`"
    private val SPECIAL_PARTS = listOf(": ", " #")
    private val TYPED_WORDS = setOf("true", "false", "null", "~", "yes", "no", "on", "off")
    val BLOCK_SCALARS = setOf("|", ">", "|-", ">-", "|+", ">+")
    val LIST_KEYS = setOf("tags", "aliases", "cssclasses", "tag", "alias")
    val NUMBER = Regex(pattern = "-?\\d+(\\.\\d+)?")

    /** What YAML reads as a timestamp: a date, alone or with a time and a zone. */
    private val DATE_LIKE =
        Regex(pattern = "\\d{4}-\\d{2}-\\d{2}([Tt ]\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?\\s*(Z|[+-]\\d{2}(:?\\d{2})?)?)?")

    fun isQuoted(
        text: String
    ): Boolean =
        text.length >= 2 && text.first() == text.last() && (text.first() == QUOTE || text.first() == APOSTROPHE)

    /** [text] without its quotes, its escapes undone. */
    fun unquoted(
        text: String
    ): String = when {
        !isQuoted(text = text) -> text
        text.first() == APOSTROPHE -> text.substring(1, text.lastIndex).replace("''", "'")
        else -> YamlEscapes.decoded(text = text.substring(1, text.lastIndex))
    }

    /** `[a, "b c"]` as its items. */
    fun inlineItems(
        text: String
    ): List<String> = scalarItems(text = text.removePrefix(prefix = INLINE_OPEN).removeSuffix(suffix = INLINE_CLOSE))

    /** Each item of a list property's [block] as written (quotes included), by its value. */
    fun writtenItems(
        block: FrontmatterBlocks.Block
    ): Map<String, String> {
        val inline = block.inlineValue
        val raw = if (inline.isNotEmpty()) {
            rawItems(text = inline.removePrefix(prefix = INLINE_OPEN).removeSuffix(suffix = INLINE_CLOSE))
        } else {
            block.continuation.map { it.trimStart() }.filter { it.startsWith(prefix = ITEM_MARK) }
                .map { it.removePrefix(prefix = ITEM_MARK).trim() }.filter { it.isNotEmpty() }
        }
        return raw.associateBy { unquoted(text = it) }
    }

    /** `a, b` as its items. */
    fun scalarItems(
        text: String
    ): List<String> = rawItems(text = text).map { unquoted(text = it) }.filter { it.isNotEmpty() }

    /**
     * The items of a comma-separated list as written, split only outside quotes. A quote opens at the start of an item
     * and nowhere else, so the apostrophe in `it's` is text.
     */
    private fun rawItems(
        text: String
    ): List<String> {
        val items = mutableListOf<String>()
        var itemStart = 0
        var quote: Char? = null
        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                quote == null && char == ITEM_SEPARATOR -> {
                    items += text.substring(startIndex = itemStart, endIndex = index)
                    itemStart = index + 1
                }

                quote == null -> if (char in QUOTES && isBlankBetween(text, itemStart, index)) quote = char

                quote == QUOTE && char == ESCAPE -> index++

                char == quote && text.getOrNull(index = index + 1) == APOSTROPHE && quote == APOSTROPHE -> index++

                char == quote -> quote = null
            }
            index++
        }
        items += text.substring(startIndex = itemStart)
        return items.map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun isBlankBetween(
        text: String,
        start: Int,
        end: Int
    ): Boolean = (start until end).all { text[it].isWhitespace() }

    /** [text] as an item of an inline list, where a comma that is not in quotes would split it in two. */
    fun writtenInline(
        text: String,
        quote: Char? = null
    ): String = written(text = text, quote = quote ?: QUOTE.takeIf { ITEM_SEPARATOR in text })

    /**
     * [text] as YAML writes it: as it is when that reads back the same text, otherwise quoted. [quote] keeps the
     * quotes a property already had (`'` or `"`), even where none are needed.
     */
    fun written(
        text: String,
        quote: Char? = null
    ): String {
        if (quote == null && !needsQuotes(text = text)) return text
        return if (quote == APOSTROPHE && !YamlEscapes.needsEscapes(text = text)) {
            APOSTROPHE + text.replace("'", "''") + APOSTROPHE
        } else {
            QUOTE + YamlEscapes.encoded(text = text) + QUOTE
        }
    }

    private fun needsQuotes(
        text: String
    ): Boolean = text.isNotEmpty() && (
        text != text.trim() || text.lowercase() in TYPED_WORDS || NUMBER.matches(input = text) ||
            DATE_LIKE.matches(input = text) || text.first() in SPECIAL_STARTS || SPECIAL_PARTS.any { it in text } ||
            text.last() == KEY_END || YamlEscapes.needsEscapes(text = text)
        )
}

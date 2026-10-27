package app.notekea.notes.domain

/** How frontmatter values are written and read back: quoting, list items and the words YAML gives a type. */
internal object YamlValues {
    const val COMMENT_MARK = "#"
    const val FALSE = "false"
    const val INLINE_CLOSE = "]"
    const val INLINE_OPEN = "["
    const val ITEM_MARK = "-"
    const val TRUE = "true"
    private const val APOSTROPHE = '\''
    private const val BACKSLASH = "\\"
    private const val ITEM_SEPARATOR = ","
    private const val QUOTE = '"'
    private const val QUOTE_TEXT = "\""
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
        else -> text.substring(1, text.lastIndex).replace("\\\"", QUOTE_TEXT).replace("\\\\", BACKSLASH)
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
            inline.removePrefix(prefix = INLINE_OPEN).removeSuffix(suffix = INLINE_CLOSE).split(ITEM_SEPARATOR)
        } else {
            block.continuation.map { it.trimStart() }.filter { it.startsWith(prefix = ITEM_MARK) }
                .map { it.removePrefix(prefix = ITEM_MARK) }
        }
        return raw.map { it.trim() }.filter { it.isNotEmpty() }.associateBy { unquoted(text = it) }
    }

    /** `a, b` as its items. */
    fun scalarItems(
        text: String
    ): List<String> = text.split(ITEM_SEPARATOR).map { unquoted(text = it.trim()) }.filter { it.isNotEmpty() }

    /**
     * [text] as YAML writes it: as it is when that reads back the same text, otherwise quoted. [quote] keeps the
     * quotes a property already had (`'` or `"`), even where none are needed.
     */
    fun written(
        text: String,
        quote: Char? = null
    ): String {
        if (quote == null && !needsQuotes(text = text)) return text
        return if (quote == APOSTROPHE) {
            APOSTROPHE + text.replace("'", "''") + APOSTROPHE
        } else {
            QUOTE + text.replace(BACKSLASH, "\\\\").replace(QUOTE_TEXT, "\\\"") + QUOTE
        }
    }

    private fun needsQuotes(
        text: String
    ): Boolean = text.isNotEmpty() && (
        text != text.trim() || text.lowercase() in TYPED_WORDS || NUMBER.matches(input = text) ||
            DATE_LIKE.matches(input = text) || text.first() in SPECIAL_STARTS || SPECIAL_PARTS.any { it in text }
        )
}

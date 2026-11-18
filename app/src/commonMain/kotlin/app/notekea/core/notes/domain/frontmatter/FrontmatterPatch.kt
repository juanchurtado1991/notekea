package app.notekea.core.notes.domain.frontmatter

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.time.LocalMoment
import app.notekea.core.domain.time.MomentFormat

/**
 * Edits one property of a note's frontmatter by rewriting only that property's lines: the other
 * properties, comments, key order, quoting and line breaks stay byte for byte. A value is written in the style the
 * property already had (a block or inline list, single or double quotes), and quoted only when YAML needs it.
 */
class FrontmatterPatch {
    private val momentFormat = MomentFormat()
    private val reader = FrontmatterReader()

    /** [header] with [key]'s value set to [value]; as it is when the note has no such property. */
    fun set(
        header: String,
        key: String,
        value: PropertyValue
    ): String {
        val frontmatter = FrontmatterBlocks.of(header = header) ?: return header
        val old = frontmatter.blockOf(key = key) ?: return header

        // The value it already has is kept as written: `record: off` was rewritten as `record: "off"`, a note's
        // frontmatter changed though nothing in it did (seen on a blind evaluation vault, 2026-11-17).
        val isSame = valueOf(
            header = header,
            key = key
        ) == value

        if (isSame) return header

        val keyText = old
            .lines
            .first()
            .substringBefore(delimiter = FrontmatterBlocks.KEY_SEPARATOR)

        val lines = linesOf(
            keyText = keyText,
            value = value,
            old = old
        )

        // Another value is its source as written, its comments included.
        val written = if (value is PropertyValue.Other) {
            lines
        } else {
            withComment(
                lines = lines,
                old = old
            )
        }

        val block = FrontmatterBlocks.Block(
            lines = written,
            key = key
        )

        return frontmatter.copy(
            blocks = frontmatter.blocks.map {
                if (it === old) block else it
            }
        ).toHeader()
    }

    private fun valueOf(
        header: String,
        key: String
    ): PropertyValue? = (reader.read(header = header) as? FrontmatterContent.Properties)
        ?.properties
        ?.firstOrNull {
            it.key == key
        }
        ?.value

    /**
     * [lines] with the comment that ended [old]'s key line (`status: draft # for now`), while the value still fits on
     * one line. Editing the value dropped it (bug hunt D10).
     */
    private fun withComment(
        lines: List<String>,
        old: FrontmatterBlocks.Block
    ): List<String> {
        val written = old
            .lines
            .first()
            .substringAfter(
                delimiter = FrontmatterBlocks.KEY_SEPARATOR,
                missingDelimiterValue = StringPatterns.EMPTY
            )
            .trim()

        val comment = written.removePrefix(prefix = YamlValues.withoutComment(text = written))
        val keepsComment = comment.isNotEmpty() && lines.size == 1

        return if (keepsComment) listOf(lines.single() + comment) else lines
    }

    /** [header] with a new property [key] at the end, a frontmatter made with [lineBreak] when the note has none. */
    fun add(
        header: String,
        key: String,
        value: PropertyValue,
        lineBreak: String = StringPatterns.NEW_LINE
    ): String {
        val frontmatter = FrontmatterBlocks.of(header = header) ?: FrontmatterBlocks(
            blocks = emptyList(),
            lineBreak = lineBreak
        )

        val block = FrontmatterBlocks.Block(
            lines = linesOf(
                keyText = key,
                value = value,
                old = null
            ),
            key = key
        )

        return frontmatter.copy(blocks = frontmatter.blocks + block).toHeader()
    }

    /** [header] without [key]; no frontmatter at all once nothing but blank lines is left. */
    fun remove(
        header: String,
        key: String
    ): String {
        val frontmatter = FrontmatterBlocks.of(header = header) ?: return header

        val blocks = frontmatter.blocks.filterNot {
            it.key == key
        }

        val isEmpty = blocks.all { block ->
            block.key == null && block.lines.all { it.isBlank() }
        }

        return if (isEmpty) frontmatter.prefix else frontmatter.copy(blocks = blocks).toHeader()
    }

    /** [header] with the key [from] renamed [to], its value untouched. */
    fun rename(
        header: String,
        from: String,
        to: String
    ): String {
        val frontmatter = FrontmatterBlocks.of(header = header) ?: return header
        val old = frontmatter.blockOf(key = from) ?: return header
        val first = old.lines.first()

        val renamed = to + FrontmatterBlocks.KEY_SEPARATOR +
            first.substringAfter(delimiter = FrontmatterBlocks.KEY_SEPARATOR)

        val block = old.copy(
            lines = listOf(renamed) + old.continuation,
            key = to
        )

        return frontmatter.copy(
            blocks = frontmatter.blocks.map {
                if (it === old) block else it
            }
        ).toHeader()
    }

    private fun linesOf(
        keyText: String,
        value: PropertyValue,
        old: FrontmatterBlocks.Block?
    ): List<String> {
        val inline = old?.inlineValue.orEmpty()

        val quote = inline.firstOrNull()?.takeIf { YamlValues.isQuoted(text = inline) }

        if (value is PropertyValue.Items) {
            return itemLines(
                keyText = keyText,
                items = value.items,
                quote = quote,
                old = old
            )
        }

        if (value is PropertyValue.Other) return value.source.split(StringPatterns.NEW_LINE)

        return listOf(
            keyLine(
                keyText = keyText,
                value = scalarText(
                    value = value,
                    quote = quote
                )
            )
        )
    }

    /** A text, number, checkbox or date value as YAML writes it. */
    private fun scalarText(
        value: PropertyValue,
        quote: Char?
    ): String = when (value) {
        is PropertyValue.Text -> YamlValues.written(
            text = value.text,
            quote = quote
        )

        is PropertyValue.Number -> value.text

        is PropertyValue.Checkbox -> if (value.isChecked) YamlValues.TRUE else YamlValues.FALSE

        is PropertyValue.Date -> momentFormat.format(
            moment = LocalMoment(date = value.date),
            pattern = ISO_DATE
        )

        is PropertyValue.Items, is PropertyValue.Other -> StringPatterns.EMPTY
    }

    /** A list in the style the property had: inline (`[a, b]`), comma-separated (`a, b`) or block items. */
    private fun itemLines(
        keyText: String,
        items: List<String>,
        quote: Char?,
        old: FrontmatterBlocks.Block?
    ): List<String> {
        // Items left as they were keep their quotes; only new ones are written, quoted when YAML needs it.
        val kept = old?.let { YamlValues.writtenItems(block = it) }.orEmpty()

        val inline = old?.inlineValue.orEmpty()

        val written = items.map { item ->
            kept[item] ?: if (inline.isEmpty()) {
                YamlValues.written(
                    text = item,
                    quote = quote
                )
            } else {
                YamlValues.writtenInline(
                    text = item,
                    quote = quote
                )
            }
        }

        val joined = written.joinToString(separator = ITEM_SEPARATOR)

        val isInlineList = inline.startsWith(prefix = YamlValues.INLINE_OPEN) ||
            becomesInlineList(
                inline = inline,
                itemCount = items.size,
                quote = quote,
                key = old?.key
            )

        return when {
            items.isEmpty() -> listOf(
                keyLine(
                    keyText = keyText,
                    value = StringPatterns.EMPTY
                )
            )

            isInlineList -> {
                listOf(
                    keyLine(
                        keyText = keyText,
                        value = YamlValues.INLINE_OPEN + joined + YamlValues.INLINE_CLOSE
                    )
                )
            }

            inline.isNotEmpty() -> listOf(
                keyLine(
                    keyText = keyText,
                    value = joined
                )
            )

            else -> listOf(
                keyLine(
                    keyText = keyText,
                    value = StringPatterns.EMPTY
                )
            ) +
                written.map {
                    indentOf(old = old) + YamlValues.ITEM_MARK + StringPatterns.SPACE + it
                }
        }
    }

    /**
     * Whether a value written on the key's line without brackets becomes an inline list: with two or more items, unless
     * it is a list key's plain comma-separated text. `tags: "work", "x"` was no YAML and `author: Ana, Bob` one text.
     */
    private fun becomesInlineList(
        inline: String,
        itemCount: Int,
        quote: Char?,
        key: String?
    ): Boolean {
        val isCommaList = key?.lowercase() in YamlValues.LIST_KEYS && quote == null

        return inline.isNotEmpty() &&
            itemCount >= MIN_LIST_ITEMS &&
            !isCommaList
    }

    /** How the property's list items were indented (`  ` by default, as Obsidian writes them). */
    private fun indentOf(old: FrontmatterBlocks.Block?): String = old
        ?.continuation
        ?.firstOrNull { it.trimStart().startsWith(prefix = YamlValues.ITEM_MARK) }
        ?.takeWhile { it.isWhitespace() } ?: DEFAULT_INDENT

    private fun keyLine(
        keyText: String,
        value: String
    ): String = if (value.isEmpty()) {
        keyText + FrontmatterBlocks.KEY_SEPARATOR
    } else {
        keyText + KEY_VALUE_SEPARATOR +
            value
    }

    private companion object {
        const val DEFAULT_INDENT = "  "
        const val ITEM_SEPARATOR = ", "
        const val MIN_LIST_ITEMS = 2
        const val KEY_VALUE_SEPARATOR = ": "
        const val ISO_DATE = MomentFormat.ISO_DATE
    }
}

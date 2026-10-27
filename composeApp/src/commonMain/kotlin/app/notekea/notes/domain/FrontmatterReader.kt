package app.notekea.notes.domain

import app.notekea.core.domain.time.MomentFormat

/**
 * Reads a note's frontmatter as properties (`ROADMAP.md` 1.14), each typed by what its text shows, as Obsidian does:
 * a list (block or inline, and tags, aliases and CSS classes always), `true` or `false`, a number, a date
 * (`YYYY-MM-DD`), or text (quotes removed). Nested values and multi-line text are [PropertyValue.Other], kept for the
 * source view. Lines that are not YAML properties make the frontmatter [FrontmatterContent.Invalid].
 */
class FrontmatterReader {
    private val momentFormat = MomentFormat()

    fun read(
        header: String
    ): FrontmatterContent {
        val frontmatter = FrontmatterBlocks.of(header = header) ?: return FrontmatterContent.None
        invalidLine(frontmatter = frontmatter)?.let { return FrontmatterContent.Invalid(line = it) }
        val properties = frontmatter.blocks.mapNotNull { block ->
            block.key?.let { NoteProperty(key = it, value = valueOf(block = block)) }
        }
        return FrontmatterContent.Properties(properties = properties)
    }

    /** The value of a property's [block], typed by its text. */
    fun valueOf(
        block: FrontmatterBlocks.Block
    ): PropertyValue {
        val inline = block.inlineValue
        val continuation = block.continuation.filter { it.isNotBlank() }
        val isList = block.key?.lowercase() in YamlValues.LIST_KEYS
        return when {
            continuation.isNotEmpty() -> continuationValue(block = block, inline = inline, continuation = continuation)

            inline in YamlValues.BLOCK_SCALARS -> PropertyValue.Other(source = block.lines.joinToString(separator = NL))

            inline.startsWith(prefix = YamlValues.INLINE_OPEN) && inline.endsWith(suffix = YamlValues.INLINE_CLOSE) ->
                PropertyValue.Items(items = YamlValues.inlineItems(text = inline))

            isList -> PropertyValue.Items(items = YamlValues.scalarItems(text = inline))

            else -> scalarValue(text = inline)
        }
    }

    /** Lines under the key: a list of items, or a nested value the form leaves to the source. */
    private fun continuationValue(
        block: FrontmatterBlocks.Block,
        inline: String,
        continuation: List<String>
    ): PropertyValue {
        val isItems = inline.isEmpty() && continuation.all { it.trimStart().startsWith(prefix = YamlValues.ITEM_MARK) }
        if (!isItems) return PropertyValue.Other(source = block.lines.joinToString(separator = NL))
        val items = continuation.map {
            YamlValues.unquoted(text = it.trimStart().removePrefix(YamlValues.ITEM_MARK).trim())
        }
        return PropertyValue.Items(items = items.filter { it.isNotEmpty() })
    }

    private fun scalarValue(
        text: String
    ): PropertyValue {
        val lowered = text.lowercase()
        return when {
            YamlValues.isQuoted(text = text) -> PropertyValue.Text(text = YamlValues.unquoted(text = text))

            lowered == YamlValues.TRUE || lowered == YamlValues.FALSE ->
                PropertyValue.Checkbox(isChecked = lowered == YamlValues.TRUE)

            YamlValues.NUMBER.matches(input = text) -> PropertyValue.Number(text = text)

            else -> momentFormat.parseDate(text = text, pattern = MomentFormat.ISO_DATE)
                ?.let { PropertyValue.Date(date = it) }
                ?: PropertyValue.Text(text = text)
        }
    }

    /** The first line that is not part of a YAML property (1-based), or null when every line is. */
    private fun invalidLine(
        frontmatter: FrontmatterBlocks
    ): Int? {
        var line = 0
        val keys = mutableSetOf<String>()
        frontmatter.blocks.forEach { block ->
            block.lines.forEachIndexed { index, text ->
                line++
                val isKeyLine = index == 0 && block.key != null
                val isDuplicate = isKeyLine && !keys.add(block.key)
                if (isDuplicate || (!isKeyLine && !isContinuation(line = text))) return line
            }
        }
        return null
    }

    private fun isContinuation(
        line: String
    ): Boolean = line.isBlank() || line.first().isWhitespace() ||
        line.startsWith(prefix = YamlValues.ITEM_MARK) || line.startsWith(prefix = YamlValues.COMMENT_MARK)

    private companion object {
        const val NL = "\n"
    }
}

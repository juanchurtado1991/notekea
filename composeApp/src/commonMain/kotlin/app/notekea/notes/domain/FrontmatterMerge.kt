package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * Merges a template's frontmatter into a note's (`ROADMAP.md` 1.12), as Obsidian does when a template with properties is
 * inserted: a property the note lacks is added with the template's value, one it has keeps its own value, and the list
 * properties (tags, aliases, CSS classes) get the template's items they lack, in a block list. The note's other lines
 * stay byte for byte, its line breaks included; only a list that gains items is written again.
 */
class FrontmatterMerge {
    /** [noteHeader] and [templateHeader] as [NoteDocument.header] holds them; the note's header with the template's. */
    fun merge(
        noteHeader: String,
        templateHeader: String
    ): String {
        val template = linesOf(header = templateHeader) ?: return noteHeader
        val note = linesOf(header = noteHeader) ?: return templateHeader.removePrefix(prefix = BYTE_ORDER_MARK)
        val lineBreak = if (noteHeader.contains(other = WINDOWS_LINE_BREAK)) WINDOWS_LINE_BREAK else LINE_BREAK
        val noteBlocks = blocksOf(lines = note).toMutableList()
        blocksOf(lines = template).filter { it.key != null }.forEach { added ->
            val index = noteBlocks.indexOfFirst { it.key == added.key }
            when {
                index < 0 -> noteBlocks += added
                added.key in LIST_KEYS -> noteBlocks[index] = withItems(block = noteBlocks[index], added = added)
            }
        }
        val prefix = if (noteHeader.startsWith(prefix = BYTE_ORDER_MARK)) BYTE_ORDER_MARK else StringPatterns.EMPTY
        val body = noteBlocks.flatMap { it.lines }
        return prefix + (listOf(DELIMITER) + body + DELIMITER).joinToString(separator = lineBreak) + lineBreak
    }

    /** The lines between the delimiters, or null for a note without frontmatter. */
    private fun linesOf(
        header: String
    ): List<String>? {
        val text = header.removePrefix(prefix = BYTE_ORDER_MARK).replace(WINDOWS_LINE_BREAK, LINE_BREAK)
        if (!text.startsWith(prefix = DELIMITER)) return null
        val lines = text.removeSuffix(suffix = LINE_BREAK).split(LINE_BREAK)
        return lines.subList(fromIndex = 1, toIndex = lines.lastIndex)
    }

    /** The lines grouped by top-level key: a key's line and the indented lines or list items under it. */
    private fun blocksOf(
        lines: List<String>
    ): List<Block> {
        val blocks = mutableListOf<Block>()
        lines.forEach { line ->
            val key = TOP_LEVEL_KEY.find(input = line)?.groupValues?.get(1)?.trim()
            val last = blocks.lastOrNull()
            if (key == null && last != null) {
                blocks[blocks.lastIndex] = last.copy(lines = last.lines + line)
            } else {
                blocks += Block(key = key, lines = listOf(line))
            }
        }
        return blocks
    }

    /** [block] with the items of [added] it lacks, as a block list; as it is when it lacks none. */
    private fun withItems(
        block: Block,
        added: Block
    ): Block {
        val items = itemsOf(block = block)
        val missing = itemsOf(block = added).filterNot { it in items }
        if (missing.isEmpty()) return block
        val key = block.lines.first().substringBefore(delimiter = KEY_SEPARATOR)
        return block.copy(lines = listOf(key + KEY_SEPARATOR) + (items + missing).map { ITEM_PREFIX + it })
    }

    /** A list property's items, written inline (`[a, b]`, `a, b`) or as a block list (`- a`). */
    private fun itemsOf(
        block: Block
    ): List<String> {
        val inline = block.lines.first().substringAfter(delimiter = KEY_SEPARATOR).trim()
            .removePrefix(prefix = INLINE_OPEN).removeSuffix(suffix = INLINE_CLOSE)
            .split(INLINE_SEPARATOR)
        val listed = block.lines.drop(n = 1).map { it.trim() }.filter { it.startsWith(prefix = LIST_MARK) }
            .map { it.removePrefix(prefix = LIST_MARK) }
        return (inline + listed).map { it.trim().trim(QUOTE, APOSTROPHE) }.filter { it.isNotEmpty() }.distinct()
    }

    /** One top-level property ([key]), or the lines before the first one (null key: comments, blank lines). */
    private data class Block(
        val lines: List<String>,
        val key: String?
    )

    companion object {
        private const val APOSTROPHE = '\''
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val DELIMITER = "---"
        private const val INLINE_CLOSE = "]"
        private const val INLINE_OPEN = "["
        private const val INLINE_SEPARATOR = ","
        private const val ITEM_PREFIX = "  - "
        private const val KEY_SEPARATOR = ":"
        private const val LINE_BREAK = StringPatterns.NEW_LINE
        private const val LIST_MARK = "-"
        private const val QUOTE = '"'
        private const val WINDOWS_LINE_BREAK = StringPatterns.WINDOWS_NEW_LINE
        private val LIST_KEYS = setOf("tags", "aliases", "cssclasses")
        private val TOP_LEVEL_KEY = Regex(pattern = "^([^\\s#-][^:]*):(\\s|$)")
    }
}

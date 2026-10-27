package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * Merges a template's frontmatter into a note's (`ROADMAP.md` 1.12), as Obsidian does when a template with properties is
 * inserted: a property the note lacks is added with the template's value, one it has keeps its own value, and the list
 * properties (tags, aliases, CSS classes) get the template's items they lack, in a block list. The note's other lines
 * stay byte for byte, its line breaks included; only a list that gains items is written again.
 */
class FrontmatterMerge {
    private val reader = FrontmatterReader()

    /** [noteHeader] and [templateHeader] as [NoteDocument.header] holds them; the note's header with the template's. */
    fun merge(
        noteHeader: String,
        templateHeader: String
    ): String {
        val template = FrontmatterBlocks.of(header = templateHeader) ?: return noteHeader
        val note = FrontmatterBlocks.of(header = noteHeader)
            ?: return templateHeader.removePrefix(prefix = BYTE_ORDER_MARK)
        val blocks = note.blocks.toMutableList()
        template.blocks.filter { it.key != null }.forEach { added ->
            val index = blocks.indexOfFirst { it.key == added.key }
            when {
                index < 0 -> blocks += added
                added.key?.lowercase() in LIST_KEYS -> blocks[index] = withItems(block = blocks[index], added = added)
            }
        }
        return note.copy(blocks = blocks, endsWithLineBreak = true).toHeader()
    }

    /** [block] with the items of [added] it lacks, as a block list; as it is when it lacks none. */
    private fun withItems(
        block: FrontmatterBlocks.Block,
        added: FrontmatterBlocks.Block
    ): FrontmatterBlocks.Block {
        val items = itemsOf(block = block)
        val missing = itemsOf(block = added).filterNot { it in items }
        if (missing.isEmpty()) return block
        val key = block.lines.first().substringBefore(delimiter = FrontmatterBlocks.KEY_SEPARATOR)
        val lines = listOf(key + FrontmatterBlocks.KEY_SEPARATOR) + (items + missing).map { ITEM_PREFIX + it }
        return block.copy(lines = lines)
    }

    private fun itemsOf(
        block: FrontmatterBlocks.Block
    ): List<String> = (reader.valueOf(block = block) as? PropertyValue.Items)?.items.orEmpty().distinct()

    companion object {
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val ITEM_PREFIX = "  - "
        private val LIST_KEYS = setOf("tags", "aliases", "cssclasses")
    }
}

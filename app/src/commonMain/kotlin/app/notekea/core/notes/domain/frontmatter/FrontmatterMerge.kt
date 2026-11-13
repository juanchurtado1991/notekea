package app.notekea.core.notes.domain.frontmatter

/**
 * Merges a template's frontmatter into a note's, as Obsidian does: a property the note lacks is added, one it has keeps
 * its value, and the list properties (tags, aliases, CSS classes) get the template's items they lack. The note's other
 * lines stay byte for byte; only a list that gains items is written again.
 */
class FrontmatterMerge {
    private val reader = FrontmatterReader()

    /** [noteHeader] and [templateHeader] as `NoteDocument.header` holds them; the note's header with the template's. */
    fun merge(
        noteHeader: String,
        templateHeader: String
    ): String {
        val template = FrontmatterBlocks.of(header = templateHeader) ?: return noteHeader

        val note = FrontmatterBlocks.of(header = noteHeader)
            ?: return templateHeader.removePrefix(prefix = BYTE_ORDER_MARK)

        val blocks = note.blocks.toMutableList()

        template
            .blocks
            .filter {
                it.key != null
            }
            .forEach { added ->
                val index = blocks.indexOfFirst {
                    it.key == added.key
                }

                when {
                    index < 0 -> blocks += added

                    added.key?.lowercase() in LIST_KEYS -> blocks[index] = withItems(
                        block = blocks[index],
                        added = added
                    )
                }
            }

        return note.copy(
            blocks = blocks,
            endsWithLineBreak = true
        ).toHeader()
    }

    /** [block] with the items of [added] it lacks, as a block list; as it is when it lacks none. */
    private fun withItems(
        block: FrontmatterBlocks.Block,
        added: FrontmatterBlocks.Block
    ): FrontmatterBlocks.Block {
        val items = itemsOf(block = block) ?: return block

        val missing = itemsOf(block = added).orEmpty().filterNot {
            it in items
        }

        if (missing.isEmpty()) return block
        val written = YamlValues.writtenItems(block = block)

        val kept = items.map {
            written[it] ?: YamlValues.written(text = it)
        }

        val key = block
            .lines
            .first()
            .substringBefore(delimiter = FrontmatterBlocks.KEY_SEPARATOR)

        val lines = listOf(key + FrontmatterBlocks.KEY_SEPARATOR) +
            (
                kept + missing.map { YamlValues.written(text = it) }
                ).map {
                ITEM_PREFIX + it
            }

        return block.copy(lines = lines)
    }

    /** The items of a list [block], each once; null when it holds anything else, which only the source can edit. */
    private fun itemsOf(block: FrontmatterBlocks.Block): List<String>? = (
        reader.valueOf(block = block) as? PropertyValue.Items
        )?.items?.distinct()

    companion object {
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val ITEM_PREFIX = "  - "
        private val LIST_KEYS = setOf("tags", "aliases", "cssclasses")
    }
}

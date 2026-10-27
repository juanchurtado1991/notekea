package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * A frontmatter block ([NoteDocument.header]) cut into its top-level properties: each [Block] is a key's line with the
 * indented lines or list items under it, and a block without a key holds the comments or blank lines before the first
 * property. [toHeader] writes it back with the same byte order mark, line breaks and closing line break, so blocks
 * left alone stay byte for byte.
 */
data class FrontmatterBlocks(
    val blocks: List<Block>,
    val prefix: String = StringPatterns.EMPTY,
    val lineBreak: String = StringPatterns.NEW_LINE,
    val endsWithLineBreak: Boolean = true
) {
    fun toHeader(): String {
        val lines = listOf(DELIMITER) + blocks.flatMap { it.lines } + DELIMITER
        val end = if (endsWithLineBreak) lineBreak else StringPatterns.EMPTY
        return prefix + lines.joinToString(separator = lineBreak) + end
    }

    /** The block of [key], or null. */
    fun blockOf(
        key: String
    ): Block? = blocks.firstOrNull { it.key == key }

    /** One top-level property ([key] as written), or the lines before the first one (null key). */
    data class Block(
        val lines: List<String>,
        val key: String?
    ) {
        /** What follows the key's colon on its first line, trimmed. */
        val inlineValue: String
            get() = lines.first().substringAfter(delimiter = KEY_SEPARATOR, missingDelimiterValue = "").trim()

        /** The lines under the key's line. */
        val continuation: List<String>
            get() = lines.drop(n = 1)
    }

    companion object {
        const val DELIMITER = "---"
        const val KEY_SEPARATOR = ":"
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private val TOP_LEVEL_KEY = Regex(pattern = "^([^\\s#-][^:]*):(\\s|$)")

        /** The blocks of [header], or null when the note has no frontmatter. */
        fun of(
            header: String
        ): FrontmatterBlocks? {
            val prefix = if (header.startsWith(prefix = BYTE_ORDER_MARK)) BYTE_ORDER_MARK else StringPatterns.EMPTY
            val windows = StringPatterns.WINDOWS_NEW_LINE
            val text = header.removePrefix(
                prefix = prefix
            ).replace(oldValue = windows, newValue = StringPatterns.NEW_LINE)
            if (!text.startsWith(prefix = DELIMITER)) return null
            val endsWithLineBreak = text.endsWith(suffix = StringPatterns.NEW_LINE)
            val lines = text.removeSuffix(suffix = StringPatterns.NEW_LINE).split(StringPatterns.NEW_LINE)
            return FrontmatterBlocks(
                blocks = blocksOf(lines = lines.subList(fromIndex = 1, toIndex = lines.lastIndex)),
                prefix = prefix,
                lineBreak = if (header.contains(other = windows)) windows else StringPatterns.NEW_LINE,
                endsWithLineBreak = endsWithLineBreak
            )
        }

        /** The key [line] starts, or null when it is not a property's first line. */
        fun keyOf(
            line: String
        ): String? = TOP_LEVEL_KEY.find(input = line)?.groupValues?.get(1)?.trim()

        private fun blocksOf(
            lines: List<String>
        ): List<Block> {
            val blocks = mutableListOf<Block>()
            lines.forEach { line ->
                val key = keyOf(line = line)
                val last = blocks.lastOrNull()
                if (key == null && last != null) {
                    blocks[blocks.lastIndex] = last.copy(lines = last.lines + line)
                } else {
                    blocks += Block(lines = listOf(line), key = key)
                }
            }
            return blocks
        }
    }
}

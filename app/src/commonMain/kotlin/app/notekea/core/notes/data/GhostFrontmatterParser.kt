package app.notekea.core.notes.data

import app.notekea.core.notes.domain.NoteMetadata
import com.ghost.serialization.Ghost
import com.ghost.serialization.decodeFromYaml
import com.ghost.serialization.yaml.exception.GhostYamlException

/**
 * Turns the YAML of a frontmatter block into [NoteMetadata] with Ghost. Ghost fails on any type mismatch and Obsidian
 * vaults mix shapes (`tags: recipe`, `tags: [a, b]`), so each key is cut out on its own, renamed to `value` and decoded
 * as a list or as text; a key that is neither is ignored, never the whole note. Legacy `tag` and `alias` are read too.
 */
class GhostFrontmatterParser {
    fun parse(yaml: String): NoteMetadata {
        val lines = yaml.lines()

        return NoteMetadata(
            tags = valuesOf(
                lines = lines,
                keys = TAG_KEYS,
                scalarSeparators = TAG_SEPARATORS
            )
                .map { it.removePrefix(prefix = TAG_PREFIX).trim() }
                .filter { it.isNotEmpty() }
                .distinct(),
            aliases = valuesOf(
                lines = lines,
                keys = ALIAS_KEYS,
                scalarSeparators = ALIAS_SEPARATORS
            ),
            title = keyBlock(
                lines = lines,
                keys = TITLE_KEYS
            )
                ?.let { decodeText(block = it) }
                ?.trim()
                ?.ifBlank { null }
        )
    }

    private fun decodeList(block: String): List<String>? = decodeOrNull {
        Ghost.decodeFromYaml<FrontmatterListDto>(yaml = block).value
    }

    private fun decodeText(block: String): String? = decodeOrNull {
        Ghost.decodeFromYaml<FrontmatterTextDto>(yaml = block).value
    }

    private fun <T> decodeOrNull(decode: () -> T?): T? = try {
        decode()
    } catch (_: GhostYamlException) {
        null
    }

    /** The first top-level line of any of [keys] and its indented continuation, with the key renamed to `value`. */
    private fun keyBlock(
        lines: List<String>,
        keys: List<String>
    ): String? {
        val start = lines.indexOfFirst { line ->
            keys.any { line.startsWith(prefix = it + KEY_SEPARATOR) }
        }

        if (start < 0) return null

        val key = keys.first { lines[start].startsWith(prefix = it + KEY_SEPARATOR) }

        val continuation = lines.drop(n = start + 1).takeWhile { isContinuation(line = it) }

        val firstLine = VALUE_KEY + lines[start].removePrefix(prefix = key)

        return (listOf(firstLine) + continuation).joinToString(separator = LINE_SEPARATOR)
    }

    private fun isContinuation(line: String): Boolean =
        line.isBlank() ||
            line.first().isWhitespace() ||
            line.startsWith(prefix = LIST_ITEM)

    private fun valuesOf(
        lines: List<String>,
        keys: List<String>,
        scalarSeparators: Regex
    ): List<String> {
        val block = keyBlock(
            lines = lines,
            keys = keys
        ) ?: return emptyList()

        val values = decodeList(block = block)
            ?: decodeText(block = block)?.split(scalarSeparators).orEmpty()

        return values
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    companion object {
        private const val KEY_SEPARATOR = ":"
        private const val LINE_SEPARATOR = "\n"
        private const val LIST_ITEM = "-"
        private const val TAG_PREFIX = "#"
        private const val VALUE_KEY = "value"
        private val ALIAS_KEYS = listOf("aliases", "alias")
        private val ALIAS_SEPARATORS = Regex(pattern = ",")
        private val TAG_KEYS = listOf("tags", "tag")
        private val TAG_SEPARATORS = Regex(pattern = "[,\\s]+")
        private val TITLE_KEYS = listOf("title")
    }
}

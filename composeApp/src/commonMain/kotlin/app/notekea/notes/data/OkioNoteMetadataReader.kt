package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NoteMetadataReader
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

/**
 * [NoteMetadataReader] on Okio: reads lines only up to the end of the frontmatter, so a long note costs no more than a
 * short one. Frontmatter is a `---` first line (a UTF-8 byte order mark is tolerated) closed by `---` or `...`. An
 * unclosed block, or one longer than [MAX_FRONTMATTER_CHARACTERS], is treated as no frontmatter, as Obsidian does.
 */
class OkioNoteMetadataReader(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val parser: GhostFrontmatterParser
) : NoteMetadataReader {
    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): NoteMetadata? = withContext(context = dispatchers.io) {
        try {
            val yaml = fileSystem
                .source(
                    file = vaultPath.toPath() / path.value
                ).buffer()
                .use { frontmatterOf(source = it) }
            yaml?.let { parser.parse(yaml = it) } ?: NoteMetadata.NONE
        } catch (_: IOException) {
            null
        }
    }

    private fun frontmatterOf(
        source: BufferedSource
    ): String? {
        val firstLine = source.readUtf8Line()?.removePrefix(prefix = BYTE_ORDER_MARK)?.trimEnd()
        if (firstLine != OPENING_FENCE) return null
        val lines = mutableListOf<String>()
        var length = 0
        while (length <= MAX_FRONTMATTER_CHARACTERS) {
            val line = source.readUtf8Line() ?: return null
            if (line.trimEnd() in CLOSING_FENCES) return lines.joinToString(separator = LINE_SEPARATOR)
            lines += line
            length += line.length
        }
        return null
    }

    companion object {
        const val MAX_FRONTMATTER_CHARACTERS = 64 * 1024
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val LINE_SEPARATOR = "\n"
        private const val OPENING_FENCE = "---"
        private val CLOSING_FENCES = setOf("---", "...")
    }
}

package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NoteMetadataReader
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TextTagFinder
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

/**
 * [NoteMetadataReader] on Okio: reads the frontmatter, then the text line by line for its `#tags` ([TextTagFinder]),
 * up to [MAX_TEXT_CHARACTERS], so a huge file costs no more than a long note. Frontmatter is a `---` first line (a
 * UTF-8 byte order mark is tolerated) closed by `---` or `...`. An unclosed block, or one longer than
 * [MAX_FRONTMATTER_CHARACTERS], is treated as no frontmatter, as Obsidian does.
 */
class OkioNoteMetadataReader(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val parser: GhostFrontmatterParser
) : NoteMetadataReader {
    private val textTagFinder = TextTagFinder()

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): NoteMetadata? = withContext(context = dispatchers.io) {
        try {
            fileSystem.source(file = vaultPath.toPath() / path.value).buffer().use { source ->
                val firstLine = source.readUtf8Line()?.removePrefix(prefix = BYTE_ORDER_MARK)
                val yaml = firstLine?.takeIf { it.trimEnd() == OPENING_FENCE }?.let { frontmatterOf(source = source) }
                val text = (if (yaml == null) sequenceOf(firstLine) else emptySequence()) + linesOf(source = source)
                val textTags = textTagFinder.tags(lines = text.filterNotNull())
                (yaml?.let { parser.parse(yaml = it) } ?: NoteMetadata.NONE).copy(textTags = textTags)
            }
        } catch (_: IOException) {
            null
        }
    }

    /** The frontmatter after its opening `---`, or null when it is not closed in time. */
    private fun frontmatterOf(
        source: BufferedSource
    ): String? {
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

    /** The lines left in [source], up to [MAX_TEXT_CHARACTERS]. */
    private fun linesOf(
        source: BufferedSource
    ): Sequence<String> {
        var read = 0
        return generateSequence { source.takeIf { read < MAX_TEXT_CHARACTERS }?.readUtf8Line() }
            .onEach { read += it.length + 1 }
    }

    companion object {
        const val MAX_FRONTMATTER_CHARACTERS = 64 * 1024
        const val MAX_TEXT_CHARACTERS = 1024 * 1024
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val LINE_SEPARATOR = "\n"
        private const val OPENING_FENCE = "---"
        private val CLOSING_FENCES = setOf("---", "...")
    }
}

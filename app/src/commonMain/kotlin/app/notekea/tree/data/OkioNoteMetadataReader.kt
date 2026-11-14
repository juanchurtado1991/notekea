package app.notekea.tree.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextTagFinder
import app.notekea.core.notes.domain.frontmatter.FrontmatterBlocks
import app.notekea.core.notes.domain.frontmatter.FrontmatterContent
import app.notekea.core.notes.domain.frontmatter.FrontmatterReader
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.tree.domain.NoteMetadataReader
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

/**
 * [NoteMetadataReader] on Okio: reads the frontmatter, then the text line by line for its tags ([TextTagFinder]) and
 * tasks ([NoteTaskCollector]), up to [MAX_TEXT_CHARACTERS]. Frontmatter is a `---` first line closed by `---` or `...`;
 * unclosed or longer than [MAX_FRONTMATTER_CHARACTERS], it is no frontmatter, as in Obsidian.
 */
class OkioNoteMetadataReader(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val parser: GhostFrontmatterParser
) : NoteMetadataReader {
    private val frontmatterReader = FrontmatterReader()
    private val textTagFinder = TextTagFinder()

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): NoteMetadata? = withContext(context = dispatchers.io) {
        try {
            fileSystem
                .source(file = vaultPath.toPath() / path.value)
                .buffer()
                .use { source ->
                    val firstLine = lineOf(source = source)?.removePrefix(prefix = BYTE_ORDER_MARK)

                    // What the frontmatter's search read: when it is never closed these are the note's text, and
                    // were lost with its tags and tasks (the code review of 2026-11-13).
                    val readAhead = mutableListOf<String>()

                    val yaml = firstLine?.takeIf {
                        it.trimEnd() == OPENING_FENCE
                    }?.let {
                        frontmatterOf(
                            source = source,
                            lines = readAhead
                        )
                    }

                    val start =
                        if (yaml == null) sequenceOf(firstLine) + readAhead else emptySequence()

                    val text = start + linesOf(source = source)

                    val tasks = NoteTaskCollector()

                    val textTags = textTagFinder.tags(
                        lines = text.filterNotNull().onEach { tasks.read(line = it) }
                    )

                    val metadata = yaml?.let { parser.parse(yaml = it) } ?: NoteMetadata.NONE

                    val properties = yaml?.let { propertiesOf(yaml = it) }.orEmpty()

                    metadata.copy(
                        textTags = textTags,
                        properties = properties,
                        tasks = tasks.tasks
                    )
                }
        } catch (_: IOException) {
            null
        }
    }

    /** The typed properties of [yaml], or none when it cannot be read as properties. */
    private fun propertiesOf(yaml: String): List<NoteProperty> {
        val delimiter = FrontmatterBlocks.DELIMITER
        val header = delimiter + LINE_SEPARATOR + yaml + LINE_SEPARATOR + delimiter

        return (
            frontmatterReader.read(
                header = header
            ) as? FrontmatterContent.Properties
            )?.properties.orEmpty()
    }

    /** The frontmatter after its opening `---`, or null when it is not closed in time. */
    private fun frontmatterOf(
        source: BufferedSource,
        lines: MutableList<String>
    ): String? {
        var length = 0

        while (length <= MAX_FRONTMATTER_CHARACTERS) {
            val line = lineOf(source = source) ?: return null

            if (line.trimEnd() in CLOSING_FENCES) {
                return lines.joinToString(separator = LINE_SEPARATOR)
            }

            lines += line
            length += line.length
        }

        return null
    }

    /**
     * The next line of [source], at most [MAX_LINE_BYTES] of it: a file that is one line of hundreds of megabytes
     * was read whole into memory to find its tags (the code review of 2026-11-13). The rest of such a line is skipped.
     */
    private fun lineOf(source: BufferedSource): String? {
        val end = source.indexOf(NEW_LINE, 0, MAX_LINE_BYTES)
        val isShort = end != -1L || !source.request(byteCount = MAX_LINE_BYTES)
        if (isShort) return source.readUtf8Line()

        val start = source.readUtf8(byteCount = MAX_LINE_BYTES)

        while (true) {
            val next = source.indexOf(NEW_LINE, 0, MAX_LINE_BYTES)

            if (next != -1L) {
                source.skip(byteCount = next + 1)
                break
            }

            if (!source.request(byteCount = MAX_LINE_BYTES)) {
                source.skip(byteCount = source.buffer.size)
                break
            }

            source.skip(byteCount = MAX_LINE_BYTES)
        }

        return start
    }

    /** The lines left in [source], up to [MAX_TEXT_CHARACTERS]. */
    private fun linesOf(source: BufferedSource): Sequence<String> {
        var read = 0

        return generateSequence {
            source.takeIf {
                read < MAX_TEXT_CHARACTERS
            }?.let { lineOf(source = it) }
        }
            .onEach { read += it.length + 1 }
    }

    companion object {
        const val MAX_FRONTMATTER_CHARACTERS = 64 * 1024
        const val MAX_TEXT_CHARACTERS = 1024 * 1024
        private const val MAX_LINE_BYTES = 1024L * 1024
        private const val NEW_LINE = '\n'.code.toByte()
        private const val BYTE_ORDER_MARK = "\uFEFF"
        private const val LINE_SEPARATOR = "\n"
        private const val OPENING_FENCE = "---"
        private val CLOSING_FENCES = setOf("---", "...")
    }
}

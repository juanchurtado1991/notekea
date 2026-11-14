package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * What is in a folder or under a tag, by name, or the notes changed last when neither is given: the vault seen as
 * its tree shows it, without reading any note (`docs/agent-loop.md`, step 4).
 */
class ListTool : ChatTool {
    override val name = NAME

    override val description =
        "a folder's name or a tag; you get the names of the notes there. With neither, the notes changed last."

    override val arguments = listOf(
        ToolArgument(
            name = FOLDER,
            isRequired = false
        ),
        ToolArgument(
            name = TAG,
            isRequired = false
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = FOLDER).ifEmpty { call.text(name = TAG) }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val folder = call.text(name = FOLDER).trim('/')
        val tag = call.text(name = TAG)
        val notes = context.notes.filter { it.file.path.isMarkdown }
        val isRecent = folder.isEmpty() && tag.isEmpty()

        val (where, found) = when {
            folder.isNotEmpty() -> "in \"$folder\"" to notes.filter {
                isIn(
                    note = it.file.path,
                    folder = folder
                )
            }

            tag.isNotEmpty() -> "tagged $tag" to notes.filter { it.hasTag(tag = tag) }

            else -> "changed last" to recent(notes = notes)
        }

        if (found.isEmpty()) return ToolResult(forModel = "list: no note $where.")

        val names = found
            .map {
                it
                    .file
                    .path
                    .value
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
            }
            .let {
                if (isRecent) it else it.sorted()
            }

        val more = (names.size - MAX_NAMES)
            .takeIf {
                it > 0
            }
            ?.let { "\n(and $it more)" }
            .orEmpty()

        return ToolResult(
            forModel = "list, ${names.size} notes $where:\n" +
                names.take(n = MAX_NAMES).joinToString(separator = "\n") + more
        )
    }

    private fun recent(notes: List<NoteEntry>): List<NoteEntry> = notes
        .filter {
            it.file.modifiedAt != null
        }
        .sortedByDescending { it.file.modifiedAt }
        .take(n = RECENT)

    // A folder is named by its path or by its last name alone, whatever its case.
    private fun isIn(
        note: RelativePath,
        folder: String
    ): Boolean {
        val parent = note
            .value
            .substringBeforeLast(
                delimiter = '/',
                missingDelimiterValue = ""
            )
            .lowercase()

        val wanted = folder.lowercase()

        return parent == wanted ||
            parent.startsWith(prefix = "$wanted/") ||
            parent.endsWith(suffix = "/$wanted") ||
            "/$wanted/" in "/$parent/"
    }

    companion object {
        const val NAME = "list"
        const val FOLDER = "folder"
        const val TAG = "tag"
        private const val MAX_NAMES = 60
        private const val RECENT = 20
    }
}

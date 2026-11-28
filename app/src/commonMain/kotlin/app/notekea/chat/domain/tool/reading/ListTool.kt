package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.chat.domain.tool.lookup.FoundRows
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * What is in a folder or under a tag, by name, the notes with no tag ([NO_TAG]), or the notes changed last when
 * neither is given: the vault seen as its tree shows it, without reading any note (`docs/agent-loop.md`, step 4).
 */
class ListTool(actionRunner: ActionRunner) : ChatTool {
    private val foundRows = FoundRows(actionRunner = actionRunner)

    override val name = NAME

    override val description =
        "a folder's name or a tag; you get the names of the notes there, each with its properties' names, " +
            "its tags and its open tasks. Tag $NO_TAG: the notes with no tag. With neither, the notes changed last."

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
        // The app's own list of the notes with no tag, a table the person can open them from.
        if (call.text(name = TAG).lowercase() == NO_TAG) {
            return foundRows.of(
                step = ActionStep(kind = ActionKind.LIST_UNTAGGED),
                context = context
            )
        }

        val notes = context.looked.filter { it.file.path.isMarkdown }
        val asked = call.text(name = FOLDER).trim('/')

        // "Tags/idea", "#idea", or a folder no note is in that is a tag: the 9B listed "Tags/idea" three times.
        val named = asked.removePrefix(prefix = "#").substringAfter(delimiter = "Tags/")
        val isTagged = notes.any { it.hasTag(tag = named) }

        val isFolder = notes.any {
            isIn(
                note = it.file.path,
                folder = asked
            )
        }

        val isTag =
            asked.isNotEmpty() &&
                !isFolder &&
                isTagged

        val folder = if (isTag) "" else asked
        val tag = if (isTag) named else call.text(name = TAG)
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
            .let { entries ->
                if (isRecent) entries else entries.sortedBy { it.file.path.value }
            }
            .map { nameOf(note = it) }

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

    // A note's name with its properties' names, its tags and its open tasks, so a task about the notes "with a
    // status", "tagged idea" or "with open tasks" is seen in the list, not read note by note.
    private fun nameOf(note: NoteEntry): String {
        val name = note
            .file
            .path
            .bare()

        val keys = note
            .metadata
            .properties
            .map { it.key }
            .filter {
                it != TAGS
            }

        val open = note
            .metadata
            .tasks
            .count {
                !it.isDone
            }

        val held = listOfNotNull(
            keys.takeIf { it.isNotEmpty() }?.joinToString(),
            note
                .allTags
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = " ") { "#$it" },
            "$open open tasks".takeIf {
                open > 0
            }
        )

        return if (held.isEmpty()) name else "$name (${held.joinToString(separator = "; ")})"
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
        const val NO_TAG = "none"
        private const val MAX_NAMES = 60
        private const val RECENT = 20
        private const val TAGS = "tags"
    }
}

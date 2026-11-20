package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath

/**
 * A note's rows numbered by the code (`docs/loop-goal-plan.md`, step 3.2): what [TallyTool] counts by id, so the
 * model names rows and never copies their figures.
 */
class RowsTool(private val noteTextCache: NoteTextCache) : ChatTool {
    override val name = NAME

    override val description =
        "a note's rows numbered R1, R2… (a table's rows with their columns, list items, Key: value lines), each " +
            "under its heading; word, when given, keeps the rows about it (a column, a heading, a label). Then " +
            "tally them by id."

    override val arguments = listOf(
        ToolArgument(name = NOTE),
        ToolArgument(
            name = WORD,
            isRequired = false
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = listOf(
        context.pathOf(name = call.text(name = NOTE)) ?: call.text(name = NOTE),
        call.text(name = WORD)
    ).filter { it.isNotEmpty() }.joinToString(separator = " › ")

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val named = call.text(name = NOTE)

        val rows = rowsOf(
            name = named,
            context = context,
            noteTextCache = noteTextCache
        ) ?: return ToolResult(forModel = "rows \"$named\": no note of that name.")

        val path = rows.first
        val word = call.text(name = WORD)

        val kept = rows
            .second
            .filter {
                NoteRows.isAbout(
                    row = it,
                    word = word
                )
            }
            .ifEmpty { rows.second }

        val note = path.nameWithoutExtension

        if (kept.isEmpty()) return ToolResult(forModel = "rows \"$note\": the note has no table, list or figures.")

        val shown = kept
            .take(n = MOST)
            .groupBy { it.heading }
            .entries
            .joinToString(separator = "\n") { (heading, under) ->
                val title = heading.ifEmpty { note }

                "## $title\n" + under.joinToString(separator = "\n") { NoteRows.shownOf(row = it) }
            }

        val more =
            if (kept.size > MOST) "\n(${kept.size - MOST} more: call rows with a word to see them.)" else ""

        return ToolResult(forModel = "rows of \"$note\" (${kept.size}):\n$shown$more")
    }

    companion object {
        const val NAME = "rows"
        const val NOTE = "note"
        const val WORD = "word"
        private const val MOST = 60

        /** The note [name] means and its rows, read as a read of the turn; null when no note is named so. */
        internal suspend fun rowsOf(
            name: String,
            context: ToolContext,
            noteTextCache: NoteTextCache
        ): Pair<RelativePath, List<NoteRows.Row>>? {
            val path = context.noteNamed(name = name) ?: return null

            val note = context.notes.firstOrNull {
                it.file.path == path
            } ?: return null

            val body = noteTextCache
                .textsOf(
                    vaultPath = context.vaultPath,
                    notes = listOf(note)
                )[path]
                ?.body
                .orEmpty()

            if (body.isNotBlank()) context.reads.add(note = path)

            return path to NoteRows.of(body = body)
        }
    }
}

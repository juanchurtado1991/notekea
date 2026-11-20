package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.LineEdit
import app.notekea.chat.domain.plan.LineMatch
import app.notekea.chat.domain.plan.TaskMatch
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath

/**
 * `change_line` (4.2): the line the call's text means is found in the note as it is now ([LineMatch]) before the
 * change is added, so the card shows the note's own line and the one written in its place. A line the note does not
 * write, several alike, or a line already as asked is said to the model, never proposed.
 */
class ChangeLineTool(
    private val noteTextCache: NoteTextCache,
    private val change: ChangeTool = ChangeTools.changeLine
) : ChatTool by change {
    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val wanted = call.text(name = ActionField.TEXT.written)
        val new = call.text(name = ActionField.LINE.written)
        val path = context.noteNamed(name = call.text(name = ActionField.NOTE.written))

        val entry = context.notes.firstOrNull {
            it.file.path == path
        }

        // What is missing or unknown the change itself says.
        val isUnsaid =
            entry == null ||
                wanted.isEmpty() ||
                new.isEmpty()

        if (path == null || isUnsaid) {
            return change.run(
                call = call,
                context = context
            )
        }

        val lines = noteTextCache
            .textsOf(
                vaultPath = context.vaultPath,
                notes = listOfNotNull(entry)
            )[path]
            ?.body
            ?.lines()
            .orEmpty()

        val note = path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

        return when (val match = LineMatch.of(lines = lines, wanted = wanted)) {
            is LineMatch.One -> changed(
                call = call,
                old = match.line,
                new = new,
                context = context
            )

            is LineMatch.Several -> ToolResult(
                forModel = "$name: not added. ${match.lines.size} lines of $note read like \"$wanted\":\n" +
                    match.lines.joinToString(separator = "\n") { it } +
                    "\nCall $name with the one the task means as text, or ask_user with them as the options when " +
                    "it does not say."
            )

            LineMatch.None -> ToolResult(
                forModel = "$name: not added. No line of $note reads like \"$wanted\". Its lines:\n" +
                    shownOf(
                        lines = lines,
                        wanted = wanted
                    ) +
                    "\nCall $name with text as the note writes the line, or say the line is not there."
            )
        }
    }

    private suspend fun changed(
        call: ToolCall,
        old: String,
        new: String,
        context: ToolContext
    ): ToolResult {
        val written = LineEdit.written(
            old = old,
            new = new
        )

        val isSame = TaskMatch.plain(text = written) == TaskMatch.plain(text = old)

        if (isSame) {
            return ToolResult(
                forModel = "$name: not added. The line already reads \"${old.trim()}\": write in line what it " +
                    "must become, or say it is already so."
            )
        }

        val resolved = call.copy(
            arguments = call.arguments +
                (ActionField.TEXT.written to old) +
                (ActionField.LINE.written to written)
        )

        return change.run(
            call = resolved,
            context = context
        )
    }

    // The note's lines that share a word with [wanted], else its first ones: what the model can write as text.
    private fun shownOf(
        lines: List<String>,
        wanted: String
    ): String {
        val words = TaskMatch
            .plain(text = wanted)
            .split(' ')
            .filter {
                it.length >= WORD_CHARS
            }

        val written = lines.filter { it.isNotBlank() }

        val sharing = written.filter { line ->
            val plain = TaskMatch.plain(text = line)

            words.any {
                it in plain
            }
        }

        return sharing
            .ifEmpty { written }
            .take(n = MOST_LINES)
            .joinToString(separator = "\n") { it.trim() }
            .ifEmpty { "(none)" }
    }

    private companion object {
        const val MOST_LINES = 15
        const val WORD_CHARS = 3
    }
}

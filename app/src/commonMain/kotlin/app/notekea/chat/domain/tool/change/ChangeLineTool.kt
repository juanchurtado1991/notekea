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
 * `change_line` (4.2): the line is found in the note as it is now ([LineMatch]) before the change is added, from the
 * frame's entry or the line written, so the card shows the note's own line and the one written in its place. A line
 * the note does not write, several alike, or a line already as asked is said to the model, never proposed.
 */
class ChangeLineTool(
    private val noteTextCache: NoteTextCache,
    private val change: ChangeTool = ChangeTools.changeLine
) : ChatTool by change {
    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val path = context.noteNamed(name = call.text(name = ActionField.NOTE.written))

        // Either may be the line as the note writes it: the models wrote the new one first.
        val said = listOf(ActionField.TEXT, ActionField.LINE)
            .map { call.text(name = it.written) }
            .filter { it.isNotEmpty() }

        val entry = context.notes.firstOrNull {
            it.file.path == path
        }

        // What is missing or unknown the change itself says.
        if (path == null ||
            entry == null ||
            said.size < 2
        ) {
            return change.run(
                call = call,
                context = context
            )
        }

        val lines = noteTextCache
            .textsOf(
                vaultPath = context.vaultPath,
                notes = listOf(entry)
            )[path]
            ?.body
            ?.lines()
            .orEmpty()

        // The entry the frame settled is the line, as the code showed it; else the one a written line means.
        val settled = context
            .frame
            ?.takeIf {
                it.meant == path
            }
            ?.entry

        val found = (listOfNotNull(settled) + said.reversed()).firstNotNullOfOrNull {
            LineMatch.of(
                lines = lines,
                wanted = it
            ) as? LineMatch.One
        } ?: return unfound(
            path = path,
            lines = lines,
            wanted = said.last()
        )

        // The new line is the one written that does not mean the line found; when both do ("Call mom tonight"
        // holds "Call mom"), the one that does not read as it.
        val meaning = said.filter {
            val match = LineMatch.of(
                lines = lines,
                wanted = it
            )

            (match as? LineMatch.One)?.at == found.at
        }

        val new = (said - meaning.toSet()).firstOrNull() ?: said.firstOrNull {
            plainOf(line = it) != plainOf(line = found.line)
        }

        return changed(
            call = call,
            old = found.line,
            new = new,
            context = context
        )
    }

    private fun unfound(
        path: RelativePath,
        lines: List<String>,
        wanted: String
    ): ToolResult {
        val note = path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

        val match = LineMatch.of(
            lines = lines,
            wanted = wanted
        )

        val said = if (match is LineMatch.Several) {
            "${match.lines.size} lines of $note read like \"$wanted\":\n" +
                match.lines.joinToString(separator = "\n") +
                "\nCall $name with the one the task means as line, or ask_user with them as the options when " +
                "it does not say."
        } else {
            "No line of $note reads like \"$wanted\". Its lines:\n" +
                shownOf(
                    lines = lines,
                    wanted = wanted
                ) +
                "\nCall $name with line as the note writes it, or say the line is not there."
        }

        return ToolResult(forModel = "$name: not added. $said")
    }

    private suspend fun changed(
        call: ToolCall,
        old: String,
        context: ToolContext,
        new: String?
    ): ToolResult {
        val written = new?.let {
            LineEdit.written(
                old = old,
                new = it
            )
        }

        val isSame = written == null || plainOf(line = written) == plainOf(line = old)

        if (written == null || isSame) {
            return ToolResult(
                forModel = "$name: not added. The line already reads \"${old.trim()}\": write in text what it " +
                    "must become, or say it is already so."
            )
        }

        val resolved = call.copy(
            arguments = call.arguments +
                (ActionField.LINE.written to old) +
                (ActionField.TEXT.written to written)
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

    private fun plainOf(line: String): String = TaskMatch.plain(text = LineEdit.textOf(line = line))

    private companion object {
        const val MOST_LINES = 15
        const val WORD_CHARS = 3
    }
}

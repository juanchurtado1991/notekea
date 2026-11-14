package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.chat.domain.tool.reading.TextOutline
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache

/**
 * Notes, words or tasks counted by the app, in the vault, a folder, a tag or one note: asked how many, the model
 * gets the number and does not guess it (`docs/agent-loop.md`, step 4).
 */
class CountTool(private val noteTextCache: NoteTextCache) : ChatTool {
    override val name = NAME

    override val description =
        "what to count (notes, words, tasks, open tasks or done tasks) and where (a folder, a #tag or a " +
            "note's name; the whole vault when left out); you get the number."

    override val arguments = listOf(
        ToolArgument(name = WHAT),
        ToolArgument(
            name = WHERE,
            isRequired = false
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = "${call.text(name = WHAT)} ${call.text(name = WHERE)}".trim()

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val what = call.text(name = WHAT).lowercase()
        val where = call.text(name = WHERE)

        val notes = notesIn(
            where = where,
            context = context
        ) ?: return ToolResult(forModel = "count: no folder, tag or note \"$where\".")

        val tasks = notes.flatMap { it.metadata.tasks }

        val count = when {
            "word" in what -> wordsIn(
                notes = notes,
                context = context
            )

            "open" in what -> tasks.count {
                !it.isDone
            }

            "done" in what -> tasks.count { it.isDone }

            "task" in what -> tasks.size

            "note" in what -> notes.size

            else -> return ToolResult(forModel = "count: I count notes, words, tasks, open tasks or done tasks.")
        }

        val place = where.ifEmpty { "the vault" }

        return ToolResult(forModel = "count: $count $what in $place.")
    }

    // The notes [where] names: a tag's, one note, a folder's; all of them when it names nothing; null when unknown.
    private fun notesIn(
        where: String,
        context: ToolContext
    ): List<NoteEntry>? {
        val all = context.notes.filter { it.file.path.isMarkdown }
        if (where.isEmpty()) return all
        if (where.startsWith(prefix = TAG_MARK)) return all.filter { it.hasTag(tag = where) }

        val folder = where
            .trim('/')
            .lowercase()

        val inFolder = all.filter {
            "/$folder/" in "/${it.file.path.value.lowercase()}"
        }

        val note = context.noteNamed(name = where)

        return when {
            inFolder.isNotEmpty() -> inFolder

            note != null -> all.filter {
                it.file.path == note
            }

            else -> null
        }
    }

    private suspend fun wordsIn(
        notes: List<NoteEntry>,
        context: ToolContext
    ): Int = noteTextCache
        .textsOf(
            vaultPath = context.vaultPath,
            notes = notes
        )
        .values
        .sumOf { TextOutline.wordsIn(text = it.body) }

    companion object {
        const val NAME = "count"
        const val WHAT = "what"
        const val WHERE = "where"
        private const val TAG_MARK = "#"
    }
}

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
import app.notekea.core.notes.domain.frontmatter.PropertyValue

/**
 * Notes, words or tasks counted by the app, in the vault, a folder, a tag or one note: asked how many, the model
 * gets the number and does not guess it (`docs/agent-loop.md`, step 4).
 */
class CountTool(private val noteTextCache: NoteTextCache) : ChatTool {
    override val name = NAME

    override val description =
        "what to count (notes, words, tasks, open tasks or done tasks), or a property to sum (servings), or a " +
            "word a note writes with a figure many times (Spent, points), and where (a folder, a #tag or a " +
            "note's name; the whole vault when left out); you get the number, or every such line and their sum."

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

        // The code review of 2026-11-22: matched as a part of a word, a property named "keywords" was
        // counted as words and "footnotes" as notes.
        val asked = what
            .split(regex = NOT_WORD)
            .map { it.removeSuffix(suffix = PLURAL) }

        val count = when {
            "word" in asked -> wordsIn(
                notes = notes,
                context = context
            )

            "open" in asked -> tasks.count {
                !it.isDone
            }

            "done" in asked -> tasks.count { it.isDone }

            "task" in asked -> tasks.size

            "note" in asked -> notes.size

            else -> return ToolResult(
                forModel = summed(
                    property = what,
                    notes = notes
                ) ?: WrittenFigures.of(
                    word = what,
                    place = where.ifEmpty { "the vault" },
                    texts = noteTextCache
                        .textsOf(
                            vaultPath = context.vaultPath,
                            notes = notes
                        )
                        .map {
                            it.key.nameWithoutExtension to it.value.body
                        },
                    task = context.task
                ) ?: "count: I count notes, words, tasks, open tasks or done tasks, sum a property, or add up " +
                    "the figures a word is written with; nothing there writes \"$what\"."
            )
        }

        val place = where.ifEmpty { "the vault" }

        return ToolResult(forModel = "count: $count $what in $place.")
    }

    // The notes [where] names: a tag's, one note, a folder's; all of them when it names nothing; null when unknown.
    private fun notesIn(
        where: String,
        context: ToolContext
    ): List<NoteEntry>? {
        val all = context.looked.filter { it.file.path.isMarkdown }
        if (where.isEmpty()) return all
        if (where.startsWith(prefix = TAG_MARK)) return all.filter { it.hasTag(tag = where) }

        val folder = where
            .trim('/')
            .lowercase()

        val inFolder = all.filter {
            "/$folder/" in "/${it.file.path.value.lowercase()}"
        }

        val note = context.lookedNamed(name = where)

        return when {
            inFolder.isNotEmpty() -> inFolder

            note != null -> all.filter {
                it.file.path == note
            }

            else -> null
        }
    }

    // "How many servings do all the recipes add up to": the property's numbers summed, each note's shown, so the
    // model neither reads every recipe nor adds them up itself (the loop's fifth run answered 10 and 4 for 16).
    private fun summed(
        property: String,
        notes: List<NoteEntry>
    ): String? {
        val values = notes.mapNotNull { note ->
            note
                .metadata
                .properties
                .firstOrNull {
                    it.key.equals(
                        other = property.trim(),
                        ignoreCase = true
                    )
                }
                ?.let { numberOf(value = it.value) }
                ?.let {
                    note.file.path.nameWithoutExtension to it
                }
        }

        if (values.isEmpty()) return null

        val total = values.sumOf { it.second }
        val each = values.joinToString { "${it.first} ${written(number = it.second)}" }

        return "count: $property sums ${written(number = total)} over ${values.size} notes ($each)."
    }

    private fun numberOf(value: PropertyValue): Double? = when (value) {
        is PropertyValue.Number -> value.text
        is PropertyValue.Text -> value.text
        else -> null
    }?.trim()?.toDoubleOrNull()

    private fun written(number: Double): String =
        if (number % 1.0 == 0.0) number.toLong().toString() else number.toString()

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
        private const val PLURAL = "s"
        private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")
    }
}

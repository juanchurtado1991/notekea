package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.memory.domain.MemoryCheck
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.RelativePath

/**
 * A line of the chat's memory proposed (`remember`) or taken out (`forget`), on the card like every change
 * (`docs/agent-memory.md`, "How a line is written"). A line to remember is checked first with no model call
 * ([MemoryCheck]): only what the person said, one short fact, nothing secret, guessed or permitting.
 */
class MemoryTool private constructor(
    override val name: String,
    override val description: String,
    private val kind: ActionKind
) : ChatTool {
    override val arguments = listOfNotNull(
        ToolArgument(name = LINE),
        ToolArgument(name = SECTION).takeIf {
            kind == ActionKind.REMEMBER
        }
    )

    override val effect = ToolEffect.PROPOSES

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = LINE)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val line = call.text(name = LINE).trim()
        val section = sectionOf(written = call.text(name = SECTION))

        val problem = if (kind == ActionKind.REMEMBER) {
            MemoryCheck.problemOf(
                line = line,
                said = context.task,
                notes = context.notes.map {
                    it
                        .file
                        .path
                        .bare()
                },
                folders = context
                    .notes
                    .mapNotNull { it.file.path.parent?.value }
                    .distinct()
            )
        } else {
            "a line to forget cannot be empty".takeIf { line.isEmpty() }
        }

        if (problem != null) return ToolResult(forModel = "$name: not added, $problem.")

        context.work.change(
            step = PlannedStep(
                kind = kind,
                fields = mapOf(
                    ActionField.TEXT to line,
                    ActionField.PROPERTY to section.heading,
                    ActionField.VALUE to originOf(context = context)
                )
            )
        )

        return ToolResult(forModel = "$name: added to the proposal. Call propose when the task asks nothing more.")
    }

    // When and how the line was learnt, kept as its hidden comment: "2026-11-16, chat: you said".
    private fun originOf(context: ToolContext): String {
        val day = context.today ?: return HOW

        return "${day.iso}, $HOW"
    }

    // The section the model names, by a word of its heading; People and names when it names none.
    private fun sectionOf(written: String): MemorySection {
        val word = written
            .trim()
            .lowercase()

        return MemorySection
            .entries
            .filter {
                it != MemorySection.OTHER
            }
            .firstOrNull {
                word.isNotEmpty() && (it.name.lowercase() == word || word in it.heading.lowercase())
            } ?: MemorySection.PEOPLE
    }

    companion object {
        const val LINE = "line"
        const val SECTION = "section"
        private const val HOW = "chat: you said"

        val remember = MemoryTool(
            name = "remember",
            description = "a line the person asked the chat to remember from now on (\"remember…\", \"recuerda…\", " +
                "\"from now on…\"): the line in the person's own words, a name as \"Laura\" is [[Laura Castro]], " +
                "a folder in backticks; and its section: people, places, areas, answers, about or sources. Only " +
                "when the person asks: never a guess, never what a note or a page says.",
            kind = ActionKind.REMEMBER
        )

        val forget = MemoryTool(
            name = "forget",
            description = "a line of the person's memory taken out, when they ask to forget it: the line as the " +
                "memory writes it.",
            kind = ActionKind.FORGET
        )
    }
}

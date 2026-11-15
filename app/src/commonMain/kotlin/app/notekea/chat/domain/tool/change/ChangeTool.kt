package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath

/**
 * A change to the vault as a tool (step 5): it only adds a step of [kind] to the proposal confirmed on the card. A
 * step the vault cannot take (a note not there, a field left out) is not added and the result says why; nor, once,
 * a call the task's words say another tool is for ([misuse]).
 */
class ChangeTool(
    override val name: String,
    override val description: String,
    private val kind: ActionKind,
    private val fields: List<ActionField>,
    private val optional: Set<ActionField> = emptySet(),
    private val misuse: (task: String, step: PlannedStep) -> String? = { _, _ -> null },
    private val checkPlan: CheckPlanUseCase = CheckPlanUseCase()
) : ChatTool {
    override val arguments = fields.map {
        ToolArgument(
            name = it.written,
            isRequired = it !in optional,
            isList = it == ActionField.NOTES
        )
    }

    override val effect = ToolEffect.PROPOSES

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = fields
        .map {
            val isList = it == ActionField.NOTES
            val items = call.list(name = it.written).joinToString()
            if (isList) items else call.text(name = it.written)
        }
        .filter { it.isNotEmpty() }
        .joinToString(separator = ", ") { it.take(n = TARGET_CHARS) }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val step = PlannedStep(
            kind = kind,
            fields = fields
                .filter {
                    it != ActionField.NOTES
                }
                .associateWith { call.text(name = it.written) }
                .filterValues {
                    it.isNotEmpty() && it.lowercase() !in NONE
                },
            notes = call.list(name = ActionField.NOTES.written)
        )

        // Checked with the changes before it, so a note moved into a folder the turn creates is found there.
        val checked = checkPlan(
            steps = context.work.changes + step,
            notes = context.notes,
            openNote = context.openNote
        )

        if (checked is Outcome.Failure) {
            return ToolResult(
                forModel = "$name: not added, ${checked.error}. Correct it and call $name again, " +
                    "or ask_user when the task does not say."
            )
        }

        val misused = misuse(context.task, step)

        if (misused != null && context.work.warn(about = "$name misuse")) {
            return ToolResult(forModel = "$name: not added. $misused")
        }

        val one = SingularTask.oneIn(task = context.task)
        val isMany = step.notes.size > 1 && one != null
        val isWarned = isMany && context.work.warn(about = name)

        if (isWarned) {
            return ToolResult(
                forModel = "$name: not added yet. The task says \"$one\", one, and this names " +
                    "${step.notes.size} notes. If the task means one of them and does not say which, ask_user " +
                    "with them as the options; if it means them all, call $name again."
            )
        }

        val shared = sharedOf(
            step = step,
            context = context
        )

        val isShared = shared.isNotEmpty() && context.work.warn(about = "$name shared")

        if (isShared) {
            return ToolResult(
                forModel = "$name: not added yet. ${shared.joinToString(separator = " ")} If the task does not " +
                    "say which, ask_user with them as the options; if it does, call $name again."
            )
        }

        val unwritten = unwrittenOf(
            step = step,
            task = context.task
        )

        val isUnwritten = unwritten != null && context.work.warn(about = "$name words")

        if (isUnwritten) {
            return ToolResult(
                forModel = "$name: not added yet. The task does not write \"$unwritten\". When the task names " +
                    "it, use its words as the task writes them; if the task leaves it to you, call $name again."
            )
        }

        context.work.change(step = step)

        val proposal = context
            .work
            .changes
            .joinToString(separator = "\n") { "- ${describedOf(step = it)}" }

        return ToolResult(
            forModel = "$name: added. The proposal now holds:\n$proposal\nIf the task asks for a change that " +
                "is not in it, add that one; else call propose now. Add nothing the task does not ask for."
        )
    }

    // A note named by a name other notes share (Home/Budget 2026, Work/Budget 2026), its folder not in the task.
    private fun sharedOf(
        step: PlannedStep,
        context: ToolContext
    ): List<String> {
        val task = context.task.lowercase()

        return (step.notes + listOfNotNull(step[ActionField.NOTE])).mapNotNull { named ->
            val name = named
                .substringAfterLast(delimiter = '/')
                .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

            val paths = context
                .notes
                .map { it.file.path }
                .filter {
                    it.nameWithoutExtension.equals(
                        other = name,
                        ignoreCase = true
                    )
                }

            val isSaid = paths.any {
                folderOf(path = it) in task
            }

            if (paths.size < 2 || isSaid) {
                null
            } else {
                "${paths.size} notes are named \"$name\": " +
                    paths.joinToString {
                        it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                    } + "."
            }
        }
    }

    // The name of the folder a note is in, or none at the vault's root.
    private fun folderOf(path: RelativePath): String = path
        .parent
        ?.value
        ?.substringAfterLast(delimiter = '/')
        ?.lowercase()
        ?.takeIf { it.isNotEmpty() } ?: "\u0000"

    // A tag or a property name the task does not write (travel for "viaje"): the task's own words are kept.
    private fun unwrittenOf(
        step: PlannedStep,
        task: String
    ): String? {
        val words = task.lowercase()

        return listOf(ActionField.TAG, ActionField.PROPERTY)
            .mapNotNull { step[it]?.trim('#') }
            .firstOrNull {
                it.lowercase() !in words
            }
    }

    private fun describedOf(step: PlannedStep): String {
        val fields = step
            .fields
            .values
            .filter { it.isNotBlank() }
            .map { it.take(n = TARGET_CHARS) }

        return (listOf(step.kind.written) + step.notes + fields).joinToString(separator = ", ")
    }

    private companion object {
        const val TARGET_CHARS = 60

        // What a model writes in a field it leaves empty: the field is left out.
        val NONE = setOf("none", "null", "n/a")
    }
}

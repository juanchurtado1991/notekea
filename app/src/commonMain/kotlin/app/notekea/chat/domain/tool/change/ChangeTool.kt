package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.FrameCount
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
 * a call the task's words say another tool is for ([misuse]); nor ever a change the task does not ask ([unasked]).
 */
class ChangeTool(
    override val name: String,
    override val description: String,
    private val kind: ActionKind,
    private val fields: List<ActionField>,
    private val optional: Set<ActionField> = emptySet(),
    private val misuse: (task: String, step: PlannedStep) -> String? = { _, _ -> null },
    private val unasked: (task: String, step: PlannedStep) -> String? = { _, _ -> null },
    private val checkPlan: CheckPlanUseCase = CheckPlanUseCase()
) : ChatTool {
    override val arguments = fields.map {
        ToolArgument(
            name = it.written,
            isRequired = it !in optional,
            isList = it == ActionField.NOTES,
            isValue = it == ActionField.VALUE
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
        .joinToString(separator = ", ")

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val called = calledOf(call = call)

        val empty = emptyOf(call = call)
        if (empty != null) return ToolResult(forModel = "$name: not added. $empty")

        val step = asWritten(
            step = called,
            task = context.task
        )

        // A change put right replaces the one it corrects, so it is checked in that one's place.
        val earlier = ChangeChecks.corrected(
            step = step,
            changes = context.work.changes
        )

        val refused = notAddedOf(
            call = call,
            step = step,
            before = context.work.changes - listOfNotNull(earlier).toSet(),
            context = context
        )

        if (refused != null) return ToolResult(forModel = refused)

        val held = heldOf(
            step = step,
            context = context
        )

        if (held != null) return ToolResult(forModel = held)

        return added(
            step = step,
            called = called,
            earlier = earlier,
            context = context
        )
    }

    private fun calledOf(call: ToolCall): PlannedStep = PlannedStep(
        kind = kind,
        fields = fields
            .filter {
                it != ActionField.NOTES
            }
            .associateWith {
                writtenOf(
                    call = call,
                    field = it
                )
            }
            .filterValues {
                it.isNotEmpty() && it.lowercase() !in NONE
            },
        notes = call.list(name = ActionField.NOTES.written)
    )

    // Why the step is not added, every time: the vault cannot take it, or the task does not ask it.
    private fun notAddedOf(
        call: ToolCall,
        step: PlannedStep,
        before: List<PlannedStep>,
        context: ToolContext
    ): String? {
        // Checked with the changes before it, so a note moved into a folder the turn creates is found there.
        val checked = checkPlan(
            steps = before + step,
            notes = context.notes,
            openNote = context.openNote
        )

        if (checked is Outcome.Failure) {
            val said = ChangeChecks.said(
                problem = checked.error,
                call = call
            )

            return "$name: not added, $said. Correct it and call $name again, or ask_user when the task does " +
                "not say."
        }

        val found = (checked as Outcome.Success).value

        val refused = refusedOf(
            step = step,
            context = context
        ) ?: ChangeChecks.impossible(
            checked = found.last(),
            before = found.dropLast(n = 1),
            context = context
        )

        return refused?.let { "$name: not added. $it" }
    }

    // A step held once for a question: several notes for one, a name notes share, a word the task does not write.
    private fun heldOf(
        step: PlannedStep,
        context: ToolContext
    ): String? {
        val many = manyOf(
            step = step,
            context = context
        )

        if (many != null) return many

        val shared = sharedOf(
            step = step,
            context = context
        )

        // A task its frame says means several ("tag both of my Goals 2027 notes") means each: nothing to ask.
        val isSeveral = context.frame?.isSeveral == true

        val isShared =
            shared.isNotEmpty() &&
                !isSeveral &&
                context.work.warn(about = "$name shared")

        if (isShared) {
            return "$name: not added yet. ${shared.joinToString(separator = " ")} If the task does not " +
                "say which, ask_user with them as the options; if it does, call $name again."
        }

        return unwrittenOf(
            step = step,
            context = context
        )
    }

    private fun added(
        step: PlannedStep,
        called: PlannedStep,
        context: ToolContext,
        earlier: PlannedStep?
    ): ToolResult {
        val replaced = earlier
            ?.let {
                context.work.replace(
                    earlier = it,
                    step = step
                )

                " It replaces the earlier ${describedOf(step = it)}."
            }
            .orEmpty()

        if (earlier == null) context.work.change(step = step)

        val proposal = context
            .work
            .changes
            .joinToString(separator = "\n") { "- ${describedOf(step = it)}" }

        val kept = step[ActionField.TAG]
            ?.takeIf {
                step != called
            }
            ?.let { " The tag is \"$it\", as the task writes it." }
            .orEmpty()

        return ToolResult(
            forModel = "$name: added.$replaced$kept The proposal now holds:\n$proposal\nIf the task asks for a " +
                "change that is not in it, add that one; else call propose now. Add nothing the task does not ask for."
        )
    }

    // Several notes named where the task's frame says it means one, told once. A new note's notes are what it is
    // written from, not what it changes: "the garden" may take two (the loop's fifth run asked which garden note for
    // "a note Garden budget").
    private fun manyOf(
        step: PlannedStep,
        context: ToolContext
    ): String? {
        val frame = context.frame?.takeIf {
            it.count == FrameCount.ONE
        } ?: return null

        val isMany = step.notes.size > 1 && kind != ActionKind.WRITE_FROM_NOTES
        val isWarned = isMany && context.work.warn(about = name)
        if (!isWarned) return null

        val one = frame
            .words
            ?.takeIf { it.isNotBlank() }
            ?.let { "\"$it\", one" } ?: "one note"

        val open =
            "If the task means one of them and does not say which, ask_user with them as the options; " +
                "if it means them all, call $name again."

        val next = frame
            .meant
            ?.let {
                "The task means ${it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)}: call " +
                    "$name again with only that note."
            } ?: open

        return "$name: not added yet. The task says $one, and this names ${step.notes.size} notes. $next"
    }

    // A note named by a name other notes share (Home/Budget 2026, Work/Budget 2026), its folder not in the task.
    private fun sharedOf(
        step: PlannedStep,
        context: ToolContext
    ): List<String> {
        val frame = context.frame

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

            // Said when the frame names one of them as meant; with no frame, nothing is held back for it.
            val isSaid = frame == null || frame.notes.count { it in paths } == 1

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

    // A change the task does not ask, every time; a call another tool is for, once.
    private fun refusedOf(
        step: PlannedStep,
        context: ToolContext
    ): String? {
        val template = ChangeChecks
            .notTemplate(
                step = step,
                context = context
            )
            ?.takeIf {
                kind == ActionKind.CREATE_NOTE
            }

        return template ?: unasked(context.task, step) ?: misuse(context.task, step)?.takeIf {
            context.work.warn(about = "$name misuse")
        }
    }

    // The tag the task writes in place of another the model wrote for it: "travel" for "con viaje", "latina" for
    // "with latin". Changed here and said, as told to change it the models wrote the same tag again.
    private fun asWritten(
        step: PlannedStep,
        task: String
    ): PlannedStep {
        val tag = step[ActionField.TAG]?.trim('#')

        val named = ChangeMisuse.tagNamed(task = task).takeIf {
            kind == ActionKind.ADD_TAG
        }

        val isOther =
            tag != null &&
                named != null &&
                tag.lowercase() !in task.lowercase()

        if (!isOther) return step

        return step.copy(fields = step.fields + (ActionField.TAG to named.orEmpty()))
    }

    // A tag or a property name the task does not write, told once: the task may leave the word to the model.
    private fun unwrittenOf(
        step: PlannedStep,
        context: ToolContext
    ): String? {
        val words = context.task.lowercase()

        val unwritten = listOf(ActionField.TAG, ActionField.PROPERTY)
            .mapNotNull { step[it]?.trim('#') }
            .firstOrNull {
                it.lowercase() !in words
            } ?: return null

        if (!context.work.warn(about = "$name words")) return null

        return "$name: not added yet. The task does not write \"$unwritten\". When the task names it, use its " +
            "words as the task writes them; if the task leaves it to you, call $name again."
    }

    // A tag, a property or a value as written, without the braces and quotes of a call that broke: "{2}", "}".
    private fun writtenOf(
        call: ToolCall,
        field: ActionField
    ): String {
        val text = call.text(name = field.written)

        return if (field in BARE) ChangeChecks.writtenOf(text = text) else text
    }

    // A field written as braces and quotes alone ("}"), said with what was written: left out, it was only "missing".
    private fun emptyOf(call: ToolCall): String? {
        val field = fields.firstOrNull {
            val written = call.text(name = it.written)

            val isEmptied = ChangeChecks.writtenOf(text = written).isEmpty()

            it in BARE &&
                written.isNotEmpty() &&
                isEmptied
        } ?: return null

        return "The ${field.written} written, \"${call.text(name = field.written)}\", holds no " +
            "${field.written}: write it as the task says it (a text, a number or true/false) and call $name again."
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
        val BARE = setOf(ActionField.TAG, ActionField.PROPERTY, ActionField.VALUE)
    }
}

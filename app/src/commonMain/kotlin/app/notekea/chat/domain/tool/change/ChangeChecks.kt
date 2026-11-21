package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.NoteFileName
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.plan.TaskMatch
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.tasks.NoteTask

/**
 * What a change tool checks of a call beyond the plan's own check (the bug hunt of 2026-11-17): a value as written,
 * a refusal said in words, a change the vault cannot take answered before the card and not at Do it, and a change
 * that corrects one already added.
 */
internal object ChangeChecks {
    private const val MOST_TASKS = 15

    // `{"value": 2}`, `{ "priority" : "high" }`: an object around the value a broken call wrote.
    private val OBJECT = Regex(
        pattern = "^\\{\\s*[\"']?[^\"'{}:]+[\"']?\\s*:\\s*(.*?)\\s*}$",
        option = RegexOption.DOT_MATCHES_ALL
    )

    /**
     * A tag, a property or a value as written, without the braces and quotes of a call that broke ("{2}", "}"); an
     * object-shaped one is its value first, as trimming its braces left `value": 2` (E1).
     */
    fun writtenOf(text: String): String {
        val inner = OBJECT
            .matchEntire(input = text.trim())
            ?.groupValues
            ?.get(index = 1) ?: text

        return inner.trim('{', '}', '"', '\'', ' ')
    }

    /** Why a step is not added, in words that name what is wrong, not a data class's text (E3). */
    fun said(
        problem: ActionProblem,
        call: ToolCall
    ): String = when (problem) {
        is ActionProblem.Missing -> {
            val field = problem.field.written

            val written = call
                .arguments[field]
                ?.trim()
                .orEmpty()

            if (written.isEmpty()) "the $field is missing" else "the $field \"$written\" says no $field"
        }

        is ActionProblem.UnknownNote -> "no note is named \"${problem.name}\""

        is ActionProblem.UnknownFolder -> "no folder is named \"${problem.name}\""

        is ActionProblem.TooManyNotes ->
            "it names ${problem.count} notes, more than the ${CheckPlanUseCase.MAX_NOTES} one change takes"

        is ActionProblem.Exists -> "a note \"${nameOf(path = problem.path)}\" is already there"

        is ActionProblem.UnknownTask -> "no open task of the note reads \"${problem.text}\""

        is ActionProblem.UnknownLine -> "no one line of the note reads \"${problem.text}\""

        is ActionProblem.Refused -> "it is refused: ${problem.reason}"

        else -> "the vault cannot take it as written"
    }

    /**
     * create_note from a note that is no template: "Lista de compras" made from another note was a copy of it, and
     * passed as one the task says what to hold (T3). A template is one the task calls so, or one in a templates folder.
     */
    fun notTemplate(
        step: PlannedStep,
        context: ToolContext
    ): String? {
        val template = step[ActionField.TEMPLATE] ?: return null
        val task = context.task.lowercase()
        val isAsked = TEMPLATE_WORD.containsMatchIn(input = task)

        val isTemplate = context
            .noteNamed(name = template)
            ?.parent
            ?.value
            ?.split('/')
            ?.any { TEMPLATE_FOLDER.matches(input = it.lowercase()) } == true

        if (isAsked || isTemplate) return null

        return "\"$template\" is no template: create_note copies a template into the new note. When the task says " +
            "what the note holds, call write_note with the notes it is written from; else call create_note " +
            "without a template."
    }

    /**
     * A change the vault cannot take, said before the card: a new note where one is, a task no open task of the note
     * reads like (T7). [checked] is the step as the plan's check found it; [before] the proposal's steps before it.
     */
    fun impossible(
        checked: ActionStep,
        before: List<ActionStep>,
        context: ToolContext
    ): String? = when (checked.kind) {
        ActionKind.CREATE_NOTE -> existing(
            step = checked,
            context = context
        )

        ActionKind.COMPLETE_TASK -> unmatched(
            step = checked,
            before = before,
            context = context
        )

        else -> null
    }

    private fun existing(
        step: ActionStep,
        context: ToolContext
    ): String? {
        val path = (step.folder ?: RelativePath.ROOT).child(
            name = NoteFileName.of(title = step.title) + RelativePath.MARKDOWN_EXTENSION
        )

        val isThere = context.notes.any {
            it
                .file
                .path
                .value
                .equals(
                    other = path.value,
                    ignoreCase = true
                )
        }

        if (!isThere) return null

        return "A note \"${nameOf(path = path)}\" is already there. To change it, use append, add_task or " +
            "set_property on it; for a new note, call create_note with another title."
    }

    private fun unmatched(
        step: ActionStep,
        before: List<ActionStep>,
        context: ToolContext
    ): String? {
        val note = step.notes.firstOrNull() ?: return null

        val indexed = context
            .notes
            .firstOrNull {
                it.file.path == note
            }
            ?.metadata
            ?.tasks
            .orEmpty()

        // A task this proposal adds first may be ticked after it.
        val added = before
            .filter {
                it.kind == ActionKind.ADD_TASK && note in it.notes
            }
            .map {
                NoteTask(
                    stateIndex = -1,
                    endIndex = -1,
                    text = it.text.orEmpty(),
                    isDone = false
                )
            }

        val tasks = indexed + added
        val wanted = step.text.orEmpty()

        val open = tasks
            .filterNot {
                it.isDone || it.text.isBlank()
            }
            .take(n = MOST_TASKS)
            .joinToString(separator = "\n") { "- ${it.text.trim()}" }
            .ifEmpty { "(none)" }

        return when (val match = TaskMatch.of(tasks = tasks, wanted = wanted)) {
            is TaskMatch.One -> null

            is TaskMatch.Several ->
                "${match.texts.size} open tasks of ${nameOf(path = note)} read like \"$wanted\":\n" +
                    match.texts.joinToString(separator = "\n") { "- $it" } +
                    "\nCall complete_task with the one the task means, or ask_user when it does not say."

            TaskMatch.None ->
                "No open task of ${nameOf(path = note)} reads like \"$wanted\". Its open tasks:\n$open\n" +
                    "Call complete_task with one of them as written, or say the task is not there."
        }
    }

    /**
     * The change [step] corrects: one of the same kind and fields whose notes it takes some from or adds some to
     * ("set status on A", then "on A and B"). It is put in that one's place, not beside it (L11).
     */
    fun corrected(
        step: PlannedStep,
        changes: List<PlannedStep>
    ): PlannedStep? = changes.lastOrNull { earlier ->
        val isAlike = earlier.kind == step.kind && earlier.fields == step.fields
        val notes = step.notes.toSet()
        val others = earlier.notes.toSet()
        val isNested = notes.containsAll(elements = others) || others.containsAll(elements = notes)

        isAlike &&
            isNested &&
            step.notes.isNotEmpty()
    }

    private fun nameOf(path: RelativePath): String =
        path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

    private val TEMPLATE_WORD = Regex(pattern = "\\b(templates?|plantillas?)\\b")
    private val TEMPLATE_FOLDER = Regex(pattern = "(_)?(templates?|plantillas?)")
}

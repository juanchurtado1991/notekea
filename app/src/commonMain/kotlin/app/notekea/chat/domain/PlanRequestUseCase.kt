package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException

/**
 * Turns what was typed in the chat into a plan of up to five steps from the fixed list of [ActionKind]s (3.9), as JSON
 * a grammar keeps valid; the model reads only the request and the vault's names ([ActionContext]). A reply that is no
 * plan is read as a question, so the chat answers it from the notes as before.
 */
class PlanRequestUseCase(
    private val llmEngine: LlmEngine,
    private val planReader: ActionPlanReader
) {
    suspend operator fun invoke(
        request: String,
        context: ActionContext
    ): Outcome<List<PlannedStep>, ChatError> {
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = ActionPrompt.messages(
                        request = request,
                        context = context
                    ),
                    options = GenerationOptions(
                        maxTokens = MAX_TOKENS,
                        temperature = 0f,
                        jsonSchema = ActionPrompt.SCHEMA
                    )
                )
                .collect { json.append(it) }
        } catch (exception: LlmException) {
            return Outcome.Failure(error = ChatError.Model(error = exception.error))
        }

        val steps = planReader
            .read(json = json.toString())
            ?.take(n = ActionPrompt.MAX_STEPS)
            .orEmpty()

        val shaped = trimmed(steps = steps).map {
            foldersNamed(
                step = rescued(step = it),
                context = context,
                request = request
            )
        }

        return Outcome.Success(value = shaped)
    }

    /**
     * [step] with what a small model wrote under the wrong field put where its action reads it: a theme or a panel
     * written as a value, the period of tasks written in the request ("show_tasks period this week").
     */
    private fun rescued(step: PlannedStep): PlannedStep {
        val stray = STRAY.firstNotNullOfOrNull {
            step[it]
        } ?: return step

        val missing = step.missing.firstOrNull {
            it in RESCUED
        }

        if (missing != null) return step.copy(fields = step.fields + (missing to stray))

        val isTasks = step.kind == ActionKind.SHOW_TASKS && step[ActionField.PERIOD] == null

        val period = PERIODS.firstOrNull {
            it in stray.lowercase()
        }

        if (!isTasks || period == null) return step

        return step.copy(fields = step.fields + (ActionField.PERIOD to period))
    }

    /**
     * [step] with a folder or a template it names among its notes moved to its own field: small models put "in
     * Meetings" or "from the Meeting template" there.
     */
    private fun foldersNamed(
        step: PlannedStep,
        context: ActionContext,
        request: String
    ): PlannedStep {
        val isNewNote =
            step.kind == ActionKind.CREATE_NOTE || step.kind == ActionKind.WRITE_FROM_NOTES

        val asked = request.lowercase()

        val said = context.folders.firstOrNull {
            " in ${it.lowercase()}" in asked || " en ${it.lowercase()}" in asked
        }

        val isSaid =
            isNewNote &&
                step[ActionField.FOLDER] == null &&
                step.notes.isEmpty()

        // "create a note Groceries in Recipes": the folder the request names, when the plan left it out.
        if (isSaid && said != null) {
            return step.copy(fields = step.fields + (ActionField.FOLDER to said))
        }

        val single = step
            .notes
            .singleOrNull()
            ?.lowercase()

        val isTemplate =
            step.kind == ActionKind.CREATE_NOTE &&
                step[ActionField.TEMPLATE] == null &&
                single?.startsWith(prefix = TEMPLATES) == true

        if (isTemplate) {
            val template = step
                .notes
                .single()
                .substringAfter(delimiter = '/')

            return step.copy(
                fields = step.fields + (ActionField.TEMPLATE to template),
                notes = emptyList()
            )
        }

        val folders = context.folders.associateBy { it.lowercase() }

        val named = step.notes.mapNotNull {
            folders[it.trim('/').lowercase()]
        }

        val isFolder =
            step[ActionField.FOLDER] == null &&
                named.size == 1 &&
                step.notes.size == 1

        if (!isFolder) return step

        return step.copy(
            fields = step.fields + (ActionField.FOLDER to named.single()),
            notes = emptyList()
        )
    }

    /**
     * [steps] without what small models add on their way: a question is only answered, and a plan that changes the
     * vault does not first find or open the notes it names (the card lists them). No plan is a question.
     */
    private fun trimmed(steps: List<PlannedStep>): List<PlannedStep> {
        // A plan that also acts is an action, its answer step left out: "move Retro October" with an answer
        // must say the note is not there, not that the notes do not answer.
        val isQuestion = steps.all {
            it.kind == ActionKind.ANSWER
        }

        if (isQuestion) return listOf(PlannedStep(kind = ActionKind.ANSWER))

        val changes = steps.any { it.kind.isConfirmed }
        if (!changes) return steps

        return steps.filterNot {
            it.kind in LOOK_UPS
        }
    }

    private companion object {
        // A plan of five steps with their fields; text to write is written later, step by step.
        const val MAX_TOKENS = 400
        const val TEMPLATES = "templates/"
        val STRAY = listOf(ActionField.VALUE, ActionField.REQUEST, ActionField.TEXT)
        val RESCUED = setOf(ActionField.THEME, ActionField.PANEL, ActionField.TEXT)
        val PERIODS = listOf("overdue", "today", "this week", "later", "no date", "done")

        val LOOK_UPS = setOf(
            ActionKind.FIND_NOTES,
            ActionKind.OPEN_NOTE,
            ActionKind.OPEN_TODAY,
            ActionKind.SEARCH,
            ActionKind.SHOW_TASKS
        )
    }
}

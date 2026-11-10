package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * What the chat does with what was typed (3.8, 3.9): the model plans it, the app checks the plan, then a question is
 * answered from the notes, notes or tasks asked for are found, what only shows something is done at once, and what
 * writes or organizes is written out first and proposed on a card, done only once confirmed.
 */
class ActOnRequestUseCase(
    private val planRequest: PlanRequestUseCase,
    private val checkPlan: CheckPlanUseCase,
    private val composeNotes: ComposeNotesUseCase,
    private val actionRunner: ActionRunner
) {
    operator fun invoke(
        request: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        context: ActionContext,
        openNote: RelativePath?
    ): Flow<ActionTurnStep> = flow {
        emit(value = ActionTurnStep.Planning)

        val planned = when (val plan = planRequest(request = request, context = context)) {
            is Outcome.Success -> plan.value
            is Outcome.Failure -> return@flow emit(value = ActionTurnStep.Answer)
        }

        val isQuestion = planned.all {
            it.kind == ActionKind.ANSWER
        }

        if (isQuestion) return@flow emit(value = ActionTurnStep.Answer)

        val checked = when (
            val steps = checkPlan(
                steps = planned.filter { it.kind != ActionKind.ANSWER },
                notes = notes,
                openNote = openNote
            )
        ) {
            is Outcome.Success -> steps.value
            is Outcome.Failure -> return@flow problem(problem = steps.error)
        }

        checked.filter {
            it.kind in FINDING
        }.forEach { step ->
            when (
                val found = actionRunner.find(vaultPath = vaultPath, step = step, notes = notes)
            ) {
                is Outcome.Success -> emit(
                    value = ActionTurnStep.Found(
                        step = step,
                        found = found.value
                    )
                )

                is Outcome.Failure -> return@flow problem(problem = found.error)
            }
        }

        val shown = checked.filter {
            !it.kind.isConfirmed && it.kind !in FINDING
        }

        shown.filter {
            it.kind == ActionKind.SET_THEME
        }.forEach { step ->
            val isKnown = actionRunner.setTheme(theme = step.theme.orEmpty())
            val unknown = ActionProblem.Missing(step.kind, ActionField.THEME)
            if (!isKnown) return@flow problem(problem = unknown)
        }

        if (shown.isNotEmpty()) emit(value = ActionTurnStep.Shown(steps = shown))

        val confirmed = checked.filter { it.kind.isConfirmed }
        if (confirmed.isEmpty()) return@flow emit(value = ActionTurnStep.Finished)

        if (confirmed.any { it.kind in COMPOSED }) emit(value = ActionTurnStep.Writing)

        val composed = confirmed.map { step ->
            when (
                val written = composeNotes(
                    step = step,
                    request = request,
                    vaultPath = vaultPath,
                    notes = notes
                )
            ) {
                is Outcome.Success -> written.value
                is Outcome.Failure -> return@flow problem(problem = written.error)
            }
        }

        emit(value = ActionTurnStep.Proposed(steps = composed))
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ActionTurnStep>.problem(
        problem: ActionProblem
    ) = emit(value = ActionTurnStep.Problem(problem = problem))

    private companion object {
        val FINDING = setOf(
            ActionKind.FIND_NOTES,
            ActionKind.SHOW_TASKS,
            ActionKind.LIST_UNTAGGED,
            ActionKind.LIST_BROKEN_LINKS
        )

        val COMPOSED =
            setOf(ActionKind.CREATE_NOTE, ActionKind.WRITE_FROM_NOTES, ActionKind.NOTES_FROM_TEXT)
    }
}

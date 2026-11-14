package app.notekea.chat.domain.plan

import app.notekea.chat.domain.agent.AgentUse
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
        openNote: RelativePath?,
        agentUse: AgentUse = AgentUse.NEVER
    ): Flow<ActionTurnStep> = flow {
        emit(value = ActionTurnStep.Planning)

        val planned = when (val plan = planRequest(request = request, context = context)) {
            is Outcome.Success -> plan.value
            is Outcome.Failure -> return@flow emit(ActionTurnStep.Answer(question = request))
        }

        val reply = replyOf(
            planned = planned,
            request = request,
            notes = notes,
            openNote = openNote
        )

        routed(agentUse, reply, planned)?.let {
            return@flow emit(value = it)
        }

        val checked = when (
            val steps = checkPlan(
                steps = planned.filter { it.kind != ActionKind.ANSWER },
                notes = notes,
                openNote = openNote
            )
        ) {
            is Outcome.Success -> steps.value
            is Outcome.Failure -> return@flow emit(value = failed(steps.error, agentUse))
        }

        checked.filter {
            it.kind in FINDING
        }.forEach { step ->
            val found = actionRunner.find(
                vaultPath = vaultPath,
                step = step,
                notes = notes
            )

            val asked = ActionTurnStep.Answer(question = request)
            if (isAskedInstead(step, checked, found)) return@flow emit(value = asked)

            when (found) {
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

        val confirmed = checked.filter { it.kind.isConfirmed }

        // With a card, what only shows something waits for its Do it too: Cancel must leave everything as it was.
        if (confirmed.isEmpty()) {
            shown.filter {
                it.kind == ActionKind.SET_THEME
            }.forEach { step ->
                val isKnown = actionRunner.setTheme(theme = step.theme.orEmpty())
                val unknown = ActionProblem.Missing(step.kind, ActionField.THEME)
                if (!isKnown) return@flow problem(problem = unknown)
            }

            if (shown.isNotEmpty()) emit(value = ActionTurnStep.Shown(steps = shown))

            return@flow emit(value = ActionTurnStep.Finished)
        }

        if (confirmed.any { it.kind in COMPOSED }) emit(value = ActionTurnStep.Writing)

        val composed = composed(
            steps = confirmed,
            request = request,
            vaultPath = vaultPath,
            notes = notes
        )

        if (composed is Outcome.Failure) return@flow problem(problem = composed.error)
        val written = (composed as Outcome.Success).value

        emit(value = ActionTurnStep.Proposed(steps = written + shown))
    }

    /**
     * Whether notes by their properties that found nothing ("what trips do I have?" in a vault with a Travel folder
     * and no property for it) are searched as any question instead, being all the plan asked.
     */
    private fun isAskedInstead(
        step: ActionStep,
        checked: List<ActionStep>,
        found: Outcome<FoundNotes, ActionProblem>
    ): Boolean {
        val isNothing = found is Outcome.Failure || (found as Outcome.Success).value.rows.isEmpty()

        return step.kind == ActionKind.FIND_NOTES &&
            checked.size == 1 &&
            isNothing
    }

    /** A plan that is a question or talk, as the step that hands it back to the chat; null for an action. */
    private fun replyOf(
        planned: List<PlannedStep>,
        request: String,
        notes: Collection<NoteEntry>,
        openNote: RelativePath?
    ): ActionTurnStep? {
        val first = planned.first()
        val isQuestion = first.kind == ActionKind.ANSWER && planned.size == 1
        val standalone = first[ActionField.REQUEST] ?: request
        if (isQuestion) return ActionTurnStep.Answer(question = standalone)
        if (first.kind == ActionKind.HELP) return ActionTurnStep.Help(request = standalone)
        if (first.kind == ActionKind.OVERVIEW) return ActionTurnStep.Overview
        if (first.kind != ActionKind.CHAT) return null

        val named = first[ActionField.NOTE]?.let {
            VaultNames(notes, openNote).targetsOf(step = first)
        }

        return ActionTurnStep.Chat(
            note = (named as? Outcome.Success)
                ?.value
                ?.notes
                ?.firstOrNull()
        )
    }

    // Help, the overview and talk stay the plan's; questions and actions go to the agent if asked. Null: act on it.
    private fun routed(
        agentUse: AgentUse,
        reply: ActionTurnStep?,
        planned: List<PlannedStep>
    ): ActionTurnStep? {
        val isWork = reply == null || reply is ActionTurnStep.Answer
        val isForAgent = isWork && agentUse == AgentUse.FOR_WORK

        // Only a plan of changes is handed on: a question's plan proposes nothing.
        val changes = planned.takeIf {
            reply == null
        }.orEmpty()

        return if (isForAgent) ActionTurnStep.ToAgent(planned = changes) else reply
    }

    // A plan that names what the vault does not hold goes to the agent where Auto allows it, else says so.
    private fun failed(
        problem: ActionProblem,
        agentUse: AgentUse
    ): ActionTurnStep {
        val isStuck = agentUse.isStuckOn(problem = problem)

        return if (isStuck) ActionTurnStep.ToAgent() else ActionTurnStep.Problem(problem = problem)
    }

    // Each step with its text written. Cards asked of several notes ("study cards of WordPress"): a note that
    // backs none is left out, and the request fails only when none backs any.
    private suspend fun kotlinx.coroutines.flow.FlowCollector<ActionTurnStep>.composed(
        steps: List<ActionStep>,
        request: String,
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): Outcome<List<ActionStep>, ActionProblem> {
        val isOfSeveral = steps.count {
            it.kind == ActionKind.MAKE_CARDS
        } > 1

        val composed = mutableListOf<ActionStep>()

        steps.forEach { step ->
            val written = composeNotes(
                step = step,
                request = request,
                vaultPath = vaultPath,
                notes = notes,
                onProgress = { emit(value = ActionTurnStep.Progress(progress = it)) }
            )

            val failure = (written as? Outcome.Failure)?.error
            val isLeftOut = isOfSeveral && failure == ActionProblem.NoCards

            when {
                written is Outcome.Success -> composed += written.value
                failure != null && !isLeftOut -> return Outcome.Failure(error = failure)
            }
        }

        if (composed.isEmpty()) return Outcome.Failure(error = ActionProblem.NoCards)

        return Outcome.Success(value = composed)
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

        val COMPOSED = setOf(
            ActionKind.CREATE_NOTE,
            ActionKind.WRITE_FROM_NOTES,
            ActionKind.NOTES_FROM_TEXT,
            ActionKind.CLIP_URL,
            ActionKind.MAKE_CARDS
        )
    }
}

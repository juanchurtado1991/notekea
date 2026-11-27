package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.core.domain.Outcome

/**
 * A proposal as the card shows it: the changes checked against the vault ([CheckPlanUseCase]) and their text written
 * out ([ComposeNotesUseCase]), the writing [report]ed as it goes; or why it cannot be shown.
 */
class ProposalCheck(
    private val checkPlan: CheckPlanUseCase,
    private val composeNotes: ComposeNotesUseCase
) {
    /** How the turn ends on [proposal]'s steps: proposed, finished on its summary when none changes, or stopped. */
    suspend fun stepOf(
        proposal: AgentProposal,
        unreadable: List<String>,
        task: String,
        vault: ProposalVault,
        report: suspend (AgentStep) -> Unit
    ): AgentStep {
        // A page is clipped only from an address the task itself writes: a note or a page read on the way cannot
        // have the agent fetch an address of its own, with what it read in it (code review, 2026-11-13).
        val confirmed = UrlsNamed.shaped(
            steps = proposal.steps.filter { it.kind.isConfirmed },
            request = task
        )

        val summary = proposal.summary.orEmpty()
        if (confirmed.isEmpty()) return AgentStep.Finished(summary = summary)

        // The notes it read to write from held no text: it says so, and writes no note from what it knows.
        val isWrittenBlind =
            unreadable.isNotEmpty() && confirmed.any { it.kind in GroundedSteps.WRITTEN }

        if (isWrittenBlind) {
            return AgentStep.Stopped(problem = ActionProblem.NothingToRead(names = unreadable))
        }

        val checked = when (
            val plan = checkPlan(steps = confirmed, notes = vault.notes, openNote = vault.openNote)
        ) {
            is Outcome.Success -> plan.value
            is Outcome.Failure -> return AgentStep.Stopped(problem = plan.error)
        }

        report(AgentStep.Writing())

        // Cards asked of several notes ("study cards of WordPress"): a note that backs none is left out, and the turn
        // stops only when none backs any.
        val isOfSeveral = checked.count {
            it.kind == ActionKind.MAKE_CARDS
        } > 1

        val composed = mutableListOf<ActionStep>()

        checked.forEach { step ->
            val written = composeNotes(
                step = step,
                request = task,
                vaultPath = vault.path,
                notes = vault.notes,
                onProgress = { report(AgentStep.Writing(progress = it)) }
            )

            val failure = written.errorOrNull()
            val isLeftOut = isOfSeveral && failure == ActionProblem.NoCards

            when {
                written is Outcome.Success -> composed += written.value
                failure != null && !isLeftOut -> return AgentStep.Stopped(problem = failure)
            }
        }

        if (composed.isEmpty()) return AgentStep.Stopped(problem = ActionProblem.NoCards)

        return AgentStep.Proposed(
            steps = composed,
            summary = summary
        )
    }
}

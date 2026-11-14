package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.core.domain.Outcome

/**
 * A proposal as the card shows it, for today's agent and for the one loop: the changes checked against the vault
 * ([CheckPlanUseCase]) and their text written out ([ComposeNotesUseCase]); or why it cannot be shown.
 */
class ProposalCheck(
    private val checkPlan: CheckPlanUseCase,
    private val composeNotes: ComposeNotesUseCase
) {
    /** How the turn ends on [move]'s steps: proposed, finished on its summary when none changes, or stopped. */
    suspend fun stepOf(
        move: AgentTaskMove,
        unreadable: List<String>,
        task: String,
        vault: ProposalVault
    ): AgentStep {
        // A page is clipped only from an address the task itself writes: a note or a page read on the way cannot
        // have the agent fetch an address of its own, with what it read in it (code review, 2026-11-13).
        val confirmed = UrlsNamed.shaped(
            steps = move.steps.filter { it.kind.isConfirmed },
            request = task
        )

        val summary = move.summary.orEmpty()
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

        val composed = checked.map { step ->
            val written = composeNotes(
                step = step,
                request = task,
                vaultPath = vault.path,
                notes = vault.notes
            )

            when (written) {
                is Outcome.Success -> written.value
                is Outcome.Failure -> return AgentStep.Stopped(problem = written.error)
            }
        }

        return AgentStep.Proposed(
            steps = composed,
            summary = summary
        )
    }
}

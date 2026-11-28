package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatAction
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.core.web.domain.SourceTrust

/** The entry once the loop's turn reached [step]: its moves listed, its notes or answer being written, how it ended. */
internal fun ChatEntry.after(step: AgentStep): ChatEntry = when (step) {
    is AgentStep.Moving -> copy(
        moves = moves + AgentMoveLine(
            move = step.move,
            target = step.target
        )
    )

    // Its notes being written, a clip's pages counted as they come; or its answer so far.
    is AgentStep.Writing -> copy(
        step = if (step.answer == null) ChatStep.COMPOSING else ChatStep.WRITING,
        progress = step.progress ?: progress,
        answer = step.answer ?: answer
    )

    is AgentStep.Proposed -> ended(outcome = TurnOutcome.ACTED).copy(
        answer = step.summary,
        action = ChatAction(
            status = ActionStatus.PROPOSED,
            steps = step.steps
        )
    )

    is AgentStep.Finished -> finished(step = step)

    is AgentStep.Asked -> ended(outcome = TurnOutcome.ANSWERED).copy(
        answer = step.question,
        options = step.options,
        remembering = step.remembering
    )

    is AgentStep.Shown -> ended(outcome = TurnOutcome.ACTED).copy(
        action = ChatAction(
            status = ActionStatus.FINISHED,
            shown = step.steps
        )
    )

    is AgentStep.SourcesFound -> ended(outcome = TurnOutcome.ANSWERED).copy(
        foundSources = step.sources,
        omittedSources = step.omitted,
        pickedSources = step
            .sources
            .filter {
                it.trust == SourceTrust.Trust.TRUSTED
            }
            .map { it.result.url }
            .toSet()
    )

    is AgentStep.Stopped -> stopped(step = step)

    // The chat's own reply is written next; a failing model is shown as a failing answer's, before the entry changes.
    is AgentStep.Replied, is AgentStep.Failed -> this
}

private fun ChatEntry.ended(outcome: TurnOutcome): ChatEntry = copy(
    step = ChatStep.DONE,
    progress = null,
    outcome = outcome
)

// "No dentist appointment was found in your notes or tasks." is the not found reply (Auto-Test 3.3-05). The notes or
// tasks found show as tables under the answer, each row opening its note.
private fun ChatEntry.finished(step: AgentStep.Finished): ChatEntry {
    if (AnswerPrompt.isNotFound(text = step.summary)) {
        return ended(outcome = TurnOutcome.NOT_FOUND).copy(
            answer = "",
            canSearchWeb = true
        )
    }

    val found = step
        .tables
        .takeIf { it.isNotEmpty() }
        ?.let {
            ChatAction(
                status = ActionStatus.FINISHED,
                found = it
            )
        }

    return ended(outcome = TurnOutcome.ANSWERED).copy(
        answer = step.summary,
        sources = step.sources,
        action = found
    )
}

// A clip of a page that links pages under it asks how much to clip, in the turn's place.
private fun ChatEntry.stopped(step: AgentStep.Stopped): ChatEntry {
    val section = (step.problem as? ActionProblem.SectionAsked)?.section

    if (section != null) {
        return ended(outcome = TurnOutcome.ANSWERED).copy(
            action = null,
            sectionAsked = section
        )
    }

    return ended(outcome = TurnOutcome.ACTED).copy(
        action = ChatAction(
            status = ActionStatus.FAILED,
            problem = step.problem ?: ActionProblem.AgentStopped
        )
    )
}

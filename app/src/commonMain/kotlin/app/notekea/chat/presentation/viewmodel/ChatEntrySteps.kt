package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep

/** The entry once its answer reached [step]: where it stands, the text so far, how it ended. */
internal fun ChatEntry.after(step: AnswerStep): ChatEntry = when (step) {
    is AnswerStep.Writing -> copy(
        step = ChatStep.WRITING,
        answer = step.text,
        sources = step.read
    )

    is AnswerStep.Offered -> copy(offered = step.request)

    is AnswerStep.Done -> copy(
        step = ChatStep.DONE,
        answer = step.answer.text,
        sources = step.answer.sources,
        outcome = TurnOutcome.ANSWERED
    )

    is AnswerStep.Failed -> copy(
        step = ChatStep.DONE,
        outcome = TurnOutcome.FAILED,
        error = step.error
    )
}

/**
 * The entry after [change], remembering the step it left when it moved on to another, so the chat lists what was done
 * with a check and what is going on under it (the developer's smoke of 2026-11-13).
 */
internal fun ChatEntry.changedBy(change: (ChatEntry) -> ChatEntry): ChatEntry {
    val changed = change(this)
    val isSameStep = changed.step == step || step == ChatStep.DONE
    if (isSameStep) return changed

    return changed.copy(passed = changed.passed + step)
}

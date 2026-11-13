package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ActionProblem
import app.notekea.chat.domain.ActionStep
import app.notekea.chat.domain.DonePlan
import app.notekea.chat.domain.FoundNotes

/**
 * What the chat did, or proposes, for a request that is not a question (3.8, 3.9): the tables it [found], the steps it
 * [shown] at once, the [steps] on the card and, once done, what was [done]; or the [problem] that stopped it.
 */
data class ChatAction(
    val status: ActionStatus,
    val found: List<FoundNotes> = emptyList(),
    val shown: List<ActionStep> = emptyList(),
    val steps: List<ActionStep> = emptyList(),
    val done: DonePlan? = null,
    val problem: ActionProblem? = null
)

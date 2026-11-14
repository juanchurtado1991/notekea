package app.notekea.chat.presentation.view

import app.notekea.chat.domain.conversation.ProposalOutcome
import app.notekea.resources.Res
import app.notekea.resources.chat_acted
import app.notekea.resources.chat_action_cancelled
import app.notekea.resources.chat_action_not_done
import app.notekea.resources.chat_action_unconfirmed
import app.notekea.resources.chat_action_undone
import org.jetbrains.compose.resources.StringResource

/** A turn of an earlier session that acted: how its proposal ended, said in a line in the card's place. */
internal fun actedOf(outcome: ProposalOutcome?): StringResource = when (outcome) {
    ProposalOutcome.PROPOSED -> Res.string.chat_action_unconfirmed
    ProposalOutcome.CANCELLED -> Res.string.chat_action_cancelled
    ProposalOutcome.UNDONE -> Res.string.chat_action_undone
    ProposalOutcome.FAILED -> Res.string.chat_action_not_done
    ProposalOutcome.DONE, null -> Res.string.chat_acted
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * Secretary of a Vienna housing coop. Written blind on 2026-11-17; never tuned on
 * (docs/eval-blind.md).
 */
internal object BlindHousingCoopCases {
    const val VAULT = "Blind Housing Coop"

    val all: List<AgentWideCase> =
        BlindHousingCoopAnswers.all + BlindHousingCoopChanges.all + BlindHousingCoopAsks.all
}

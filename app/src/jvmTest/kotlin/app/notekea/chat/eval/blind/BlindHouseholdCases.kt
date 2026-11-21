package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** Toronto couple, money+home. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindHouseholdCases {
    const val VAULT = "Blind Household"

    val all: List<AgentWideCase> =
        BlindHouseholdAnswers.all + BlindHouseholdChanges.all + BlindHouseholdAsks.all
}

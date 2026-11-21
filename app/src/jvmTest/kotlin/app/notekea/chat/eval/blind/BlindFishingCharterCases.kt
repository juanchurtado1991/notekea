package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A Keys charter captain. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindFishingCharterCases {
    const val VAULT = "Blind Fishing Charter"

    val all: List<AgentWideCase> = BlindFishingCharterAnswers.all +
        BlindFishingCharterChanges.all +
        BlindFishingCharterAsks.all
}

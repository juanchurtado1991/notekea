package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** Guadalajara taxi driver. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindConductorCases {
    const val VAULT = "Blind Conductor"

    val all: List<AgentWideCase> =
        BlindConductorAnswers.all + BlindConductorChanges.all + BlindConductorAsks.all
}

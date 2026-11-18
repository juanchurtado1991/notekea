package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A beekeeper in Granada. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindApicultorCases {
    const val VAULT = "Blind Apicultor"

    val all: List<AgentWideCase> =
        BlindApicultorAnswers.all + BlindApicultorChanges.all + BlindApicultorAsks.all
}

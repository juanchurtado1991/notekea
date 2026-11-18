package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A coffee farm in Tarrazú. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindCafetalCases {
    const val VAULT = "Blind Cafetal"

    val all: List<AgentWideCase> =
        BlindCafetalAnswers.all + BlindCafetalChanges.all + BlindCafetalAsks.all
}

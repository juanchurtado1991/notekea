package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** Workshop owner, Medellín. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindTallerCases {
    const val VAULT = "Blind Taller"

    val all: List<AgentWideCase> =
        BlindTallerAnswers.all + BlindTallerChanges.all + BlindTallerAsks.all
}

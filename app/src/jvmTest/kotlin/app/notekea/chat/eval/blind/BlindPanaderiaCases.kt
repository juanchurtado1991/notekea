package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A baker in Arequipa, Peru. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindPanaderiaCases {
    const val VAULT = "Blind Panaderia"

    val all: List<AgentWideCase> =
        BlindPanaderiaAnswers.all + BlindPanaderiaChanges.all + BlindPanaderiaAsks.all
}

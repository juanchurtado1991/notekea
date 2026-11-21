package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Valencia family moving to Toronto.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindMudanzaCases {
    const val VAULT = "Blind Mudanza"

    val all: List<AgentWideCase> =
        BlindMudanzaAnswers.all + BlindMudanzaChanges.all + BlindMudanzaAsks.all
}

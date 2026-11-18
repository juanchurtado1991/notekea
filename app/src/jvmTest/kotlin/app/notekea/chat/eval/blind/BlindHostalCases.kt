package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A hostel owner in Cusco. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindHostalCases {
    const val VAULT = "Blind Hostal"

    val all: List<AgentWideCase> =
        BlindHostalAnswers.all + BlindHostalChanges.all + BlindHostalAsks.all
}

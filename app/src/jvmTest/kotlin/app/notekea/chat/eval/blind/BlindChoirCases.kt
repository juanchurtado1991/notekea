package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The director of a community choir in Cardiff.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindChoirCases {
    const val VAULT = "Blind Choir"

    val all: List<AgentWideCase> =
        BlindChoirAnswers.all + BlindChoirChanges.all + BlindChoirAsks.all
}

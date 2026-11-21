package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The activities coordinator of a Leeds care home.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindCareHomeCases {
    const val VAULT = "Blind Care Home"

    val all: List<AgentWideCase> =
        BlindCareHomeAnswers.all + BlindCareHomeChanges.all + BlindCareHomeAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A seed-stage logistics founder in Austin. Written blind on 2026-11-17; never tuned on
 * (docs/eval-blind.md).
 */
internal object BlindFounderCases {
    const val VAULT = "Blind Founder"

    val all: List<AgentWideCase> =
        BlindFounderAnswers.all + BlindFounderChanges.all + BlindFounderAsks.all
}

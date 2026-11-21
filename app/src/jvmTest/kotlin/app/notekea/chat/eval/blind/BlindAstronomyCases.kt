package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * Amateur astronomer and astronomy club treasurer in Arizona. Written blind on 2026-11-17; never
 * tuned on (docs/eval-blind.md).
 */
internal object BlindAstronomyCases {
    const val VAULT = "Blind Astronomy"

    val all: List<AgentWideCase> =
        BlindAstronomyAnswers.all + BlindAstronomyChanges.all + BlindAstronomyAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Leeds indie band's manager-guitarist on a UK tour.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindTouringBandCases {
    const val VAULT = "Blind Touring Band"

    val all: List<AgentWideCase> =
        BlindTouringBandAnswers.all + BlindTouringBandChanges.all + BlindTouringBandAsks.all
}

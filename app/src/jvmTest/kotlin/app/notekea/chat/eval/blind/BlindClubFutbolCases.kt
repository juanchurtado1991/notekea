package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The volunteer treasurer of a neighbourhood football club in Montevideo. Written blind on
 * 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindClubFutbolCases {
    const val VAULT = "Blind Club Futbol"

    val all: List<AgentWideCase> =
        BlindClubFutbolAnswers.all + BlindClubFutbolChanges.all + BlindClubFutbolAsks.all
}

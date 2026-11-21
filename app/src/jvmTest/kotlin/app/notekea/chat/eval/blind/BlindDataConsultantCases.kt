package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A freelance data scientist in Berlin.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindDataConsultantCases {
    const val VAULT = "Blind Data Consultant"

    val all: List<AgentWideCase> = BlindDataConsultantAnswers.all +
        BlindDataConsultantChanges.all +
        BlindDataConsultantAsks.all
}

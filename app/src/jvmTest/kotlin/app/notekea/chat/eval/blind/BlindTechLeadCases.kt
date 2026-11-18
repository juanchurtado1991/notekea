package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A fintech team's tech lead. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindTechLeadCases {
    const val VAULT = "Blind Tech Lead"

    val all: List<AgentWideCase> =
        BlindTechLeadAnswers.all + BlindTechLeadChanges.all + BlindTechLeadAsks.all
}

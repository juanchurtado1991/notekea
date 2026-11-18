package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** Denver runner, knee rehab. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindHealthLogCases {
    const val VAULT = "Blind Health Log"

    val all: List<AgentWideCase> =
        BlindHealthLogAnswers.all + BlindHealthLogChanges.all + BlindHealthLogAsks.all
}

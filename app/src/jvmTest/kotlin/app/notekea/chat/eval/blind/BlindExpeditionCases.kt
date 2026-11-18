package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * An amateur mountaineer in Boulder planning Mera Peak.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindExpeditionCases {
    const val VAULT = "Blind Expedition"

    val all: List<AgentWideCase> =
        BlindExpeditionAnswers.all + BlindExpeditionChanges.all + BlindExpeditionAsks.all
}

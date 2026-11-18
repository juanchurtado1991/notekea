package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Buenos Aires wedding planner running three weddings at once.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindBodasCases {
    const val VAULT = "Blind Bodas"

    val all: List<AgentWideCase> =
        BlindBodasAnswers.all + BlindBodasChanges.all + BlindBodasAsks.all
}

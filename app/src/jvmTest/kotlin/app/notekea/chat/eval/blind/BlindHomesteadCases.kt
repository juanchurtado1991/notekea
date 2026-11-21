package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Tennessee homesteader with goats, hens and a garden.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindHomesteadCases {
    const val VAULT = "Blind Homestead"

    val all: List<AgentWideCase> =
        BlindHomesteadAnswers.all + BlindHomesteadChanges.all + BlindHomesteadAsks.all
}

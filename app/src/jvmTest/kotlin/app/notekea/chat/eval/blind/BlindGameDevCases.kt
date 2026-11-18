package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A solo indie game developer in Montreal shipping a pixel-art game.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindGameDevCases {
    const val VAULT = "Blind Game Dev"

    val all: List<AgentWideCase> =
        BlindGameDevAnswers.all + BlindGameDevChanges.all + BlindGameDevAsks.all
}

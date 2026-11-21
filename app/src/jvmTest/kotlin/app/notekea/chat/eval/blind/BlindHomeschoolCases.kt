package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A parent homeschooling three kids in New Zealand.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindHomeschoolCases {
    const val VAULT = "Blind Homeschool"

    val all: List<AgentWideCase> =
        BlindHomeschoolAnswers.all + BlindHomeschoolChanges.all + BlindHomeschoolAsks.all
}

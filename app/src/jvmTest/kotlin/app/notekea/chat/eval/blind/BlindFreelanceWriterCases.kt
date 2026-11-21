package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * Orla Keane, freelance journalist in Dublin. Written blind on 2026-11-17; never tuned on
 * (docs/eval-blind.md).
 */
internal object BlindFreelanceWriterCases {
    const val VAULT = "Blind Freelance Writer"

    val all: List<AgentWideCase> = BlindFreelanceWriterAnswers.all +
        BlindFreelanceWriterChanges.all + BlindFreelanceWriterAsks.all
}

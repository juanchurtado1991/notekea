package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A tattoo artist who owns a small studio in Berlin.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindTattooStudioCases {
    const val VAULT = "Blind Tattoo Studio"

    val all: List<AgentWideCase> =
        BlindTattooStudioAnswers.all + BlindTattooStudioChanges.all + BlindTattooStudioAsks.all
}

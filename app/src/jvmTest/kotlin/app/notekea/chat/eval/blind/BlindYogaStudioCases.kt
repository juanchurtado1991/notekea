package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A yoga studio owner in Sydney. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindYogaStudioCases {
    const val VAULT = "Blind Yoga Studio"

    val all: List<AgentWideCase> =
        BlindYogaStudioAnswers.all + BlindYogaStudioChanges.all + BlindYogaStudioAsks.all
}

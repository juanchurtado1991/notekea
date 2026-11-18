package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** Atlanta photographer. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindPhotographerCases {
    const val VAULT = "Blind Photographer"

    val all: List<AgentWideCase> =
        BlindPhotographerAnswers.all + BlindPhotographerChanges.all + BlindPhotographerAsks.all
}

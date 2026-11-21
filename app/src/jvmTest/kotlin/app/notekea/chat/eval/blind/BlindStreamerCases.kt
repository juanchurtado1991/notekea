package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A game streamer in Bogotá. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindStreamerCases {
    const val VAULT = "Blind Streamer"

    val all: List<AgentWideCase> =
        BlindStreamerAnswers.all + BlindStreamerChanges.all + BlindStreamerAsks.all
}

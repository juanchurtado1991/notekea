package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A carpenter in Bilbao making custom furniture.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindCarpinteroCases {
    const val VAULT = "Blind Carpintero"

    val all: List<AgentWideCase> =
        BlindCarpinteroAnswers.all + BlindCarpinteroChanges.all + BlindCarpinteroAsks.all
}

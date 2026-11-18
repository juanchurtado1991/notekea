package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A parent in Bogotá with a family of five.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindFamiliaCases {
    const val VAULT = "Blind Familia"

    val all: List<AgentWideCase> =
        BlindFamiliaAnswers.all + BlindFamiliaChanges.all + BlindFamiliaAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The owner of a wine shop in Mendoza, Argentina.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindVinotecaCases {
    const val VAULT = "Blind Vinoteca"

    val all: List<AgentWideCase> =
        BlindVinotecaAnswers.all + BlindVinotecaChanges.all + BlindVinotecaAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * An architect in Santiago with four residential projects.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindArquitectaCases {
    const val VAULT = "Blind Arquitecta"

    val all: List<AgentWideCase> =
        BlindArquitectaAnswers.all + BlindArquitectaChanges.all + BlindArquitectaAsks.all
}

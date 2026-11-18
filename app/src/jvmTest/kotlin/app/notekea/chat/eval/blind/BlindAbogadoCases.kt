package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A solo lawyer in Monterrey. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindAbogadoCases {
    const val VAULT = "Blind Abogado"

    val all: List<AgentWideCase> =
        BlindAbogadoAnswers.all + BlindAbogadoChanges.all + BlindAbogadoAsks.all
}

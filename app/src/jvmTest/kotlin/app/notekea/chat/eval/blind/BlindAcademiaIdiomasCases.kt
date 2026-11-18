package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The director of a language academy in Sevilla. Written blind on 2026-11-17; never tuned on
 * (docs/eval-blind.md).
 */
internal object BlindAcademiaIdiomasCases {
    const val VAULT = "Blind Academia Idiomas"

    val all: List<AgentWideCase> = BlindAcademiaIdiomasAnswers.all +
        BlindAcademiaIdiomasChanges.all +
        BlindAcademiaIdiomasAsks.all
}

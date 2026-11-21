package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * Manager of a small agave and corn cooperative in Oaxaca, Mexico.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindCooperativaCases {
    const val VAULT = "Blind Cooperativa"

    val all: List<AgentWideCase> =
        BlindCooperativaAnswers.all + BlindCooperativaChanges.all + BlindCooperativaAsks.all
}

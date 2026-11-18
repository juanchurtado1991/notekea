package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A pharmacist who owns a small pharmacy in Valparaíso, Chile.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindFarmaciaCases {
    const val VAULT = "Blind Farmacia"

    val all: List<AgentWideCase> =
        BlindFarmaciaAnswers.all + BlindFarmaciaChanges.all + BlindFarmaciaAsks.all
}

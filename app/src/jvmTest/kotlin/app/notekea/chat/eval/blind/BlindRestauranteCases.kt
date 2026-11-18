package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** Guadalajara restaurateur. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindRestauranteCases {
    const val VAULT = "Blind Restaurante"

    val all: List<AgentWideCase> =
        BlindRestauranteAnswers.all + BlindRestauranteChanges.all + BlindRestauranteAsks.all
}

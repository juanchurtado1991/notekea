package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A bike mechanic's repair shop in Amsterdam. Written blind on 2026-11-17; never tuned on. */
internal object BlindBikeShopCases {
    const val VAULT = "Blind Bike Shop"

    val all: List<AgentWideCase> =
        BlindBikeShopAnswers.all + BlindBikeShopChanges.all + BlindBikeShopAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Madrid property manager running 8 rental flats for five owners.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindInmueblesCases {
    const val VAULT = "Blind Inmuebles"

    val all: List<AgentWideCase> =
        BlindInmueblesAnswers.all + BlindInmueblesChanges.all + BlindInmueblesAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A florist in Barcelona with a shop and event work.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindFloristeriaCases {
    const val VAULT = "Blind Floristeria"

    val all: List<AgentWideCase> =
        BlindFloristeriaAnswers.all + BlindFloristeriaChanges.all + BlindFloristeriaAsks.all
}

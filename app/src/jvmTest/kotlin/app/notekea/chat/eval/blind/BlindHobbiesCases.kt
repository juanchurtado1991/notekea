package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A retired teacher in Lisbon: trips, book club, Japanese, garden, photography, volunteering.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindHobbiesCases {
    const val VAULT = "Blind Hobbies"

    val all: List<AgentWideCase> =
        BlindHobbiesAnswers.all + BlindHobbiesChanges.all + BlindHobbiesAsks.all
}

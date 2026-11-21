package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A CrossFit gym owner in Denver. Written blind on 2026-11-17; never tuned on
 * (docs/eval-blind.md).
 */
internal object BlindGymOwnerCases {
    const val VAULT = "Blind Gym Owner"

    val all: List<AgentWideCase> =
        BlindGymOwnerAnswers.all + BlindGymOwnerChanges.all + BlindGymOwnerAsks.all
}

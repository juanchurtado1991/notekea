package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A daughter in Ohio caring for both elderly parents.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindCaregiverCases {
    const val VAULT = "Blind Caregiver"

    val all: List<AgentWideCase> =
        BlindCaregiverAnswers.all + BlindCaregiverChanges.all + BlindCaregiverAsks.all
}

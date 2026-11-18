package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A PGY-2 internal medicine resident in Boston.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindResidentDoctorCases {
    const val VAULT = "Blind Resident Doctor"

    val all: List<AgentWideCase> = BlindResidentDoctorAnswers.all +
        BlindResidentDoctorChanges.all +
        BlindResidentDoctorAsks.all
}

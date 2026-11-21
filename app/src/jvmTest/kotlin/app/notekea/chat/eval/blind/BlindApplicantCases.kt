package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Seattle high school senior applying to universities. Written blind on 2026-11-17; never tuned
 * on (docs/eval-blind.md).
 */
internal object BlindApplicantCases {
    const val VAULT = "Blind Applicant"

    val all: List<AgentWideCase> =
        BlindApplicantAnswers.all + BlindApplicantChanges.all + BlindApplicantAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A second-year marine biology PhD student.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindGradStudentCases {
    const val VAULT = "Blind Grad Student"

    val all: List<AgentWideCase> =
        BlindGradStudentAnswers.all + BlindGradStudentChanges.all + BlindGradStudentAsks.all
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A Chicago high school history teacher with four classes.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindTeacherCases {
    const val VAULT = "Blind Teacher"

    val all: List<AgentWideCase> =
        BlindTeacherAnswers.all + BlindTeacherChanges.all + BlindTeacherAsks.all
}

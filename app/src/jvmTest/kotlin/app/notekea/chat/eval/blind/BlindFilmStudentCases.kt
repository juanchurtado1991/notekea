package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A film student in Los Angeles making a thesis short.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindFilmStudentCases {
    const val VAULT = "Blind Film Student"

    val all: List<AgentWideCase> =
        BlindFilmStudentAnswers.all + BlindFilmStudentChanges.all + BlindFilmStudentAsks.all
}

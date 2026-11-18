package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** An ICU nurse in Manchester. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindNurseCases {
    const val VAULT = "Blind Nurse"

    val all: List<AgentWideCase> =
        BlindNurseAnswers.all + BlindNurseChanges.all + BlindNurseAsks.all
}

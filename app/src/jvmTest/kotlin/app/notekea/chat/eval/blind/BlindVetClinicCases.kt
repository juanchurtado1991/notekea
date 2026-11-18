package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A Vermont vet's clinic. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindVetClinicCases {
    const val VAULT = "Blind Vet Clinic"

    val all: List<AgentWideCase> =
        BlindVetClinicAnswers.all + BlindVetClinicChanges.all + BlindVetClinicAsks.all
}

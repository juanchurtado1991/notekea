package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/** A Perth electrician. Written blind on 2026-11-17; never tuned on (docs/eval-blind.md). */
internal object BlindElectricianCases {
    const val VAULT = "Blind Electrician"

    val all: List<AgentWideCase> =
        BlindElectricianAnswers.all + BlindElectricianChanges.all + BlindElectricianAsks.all
}

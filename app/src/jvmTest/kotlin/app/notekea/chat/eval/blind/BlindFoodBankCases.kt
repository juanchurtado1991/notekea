package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A food bank volunteer coordinator in Philadelphia.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindFoodBankCases {
    const val VAULT = "Blind Food Bank"

    val all: List<AgentWideCase> =
        BlindFoodBankAnswers.all + BlindFoodBankChanges.all + BlindFoodBankAsks.all
}

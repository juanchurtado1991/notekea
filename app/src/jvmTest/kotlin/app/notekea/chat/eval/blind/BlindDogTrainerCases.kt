package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A dog trainer and border collie breeder in Eugene, Oregon.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindDogTrainerCases {
    const val VAULT = "Blind Dog Trainer"

    val all: List<AgentWideCase> =
        BlindDogTrainerAnswers.all + BlindDogTrainerChanges.all + BlindDogTrainerAsks.all
}

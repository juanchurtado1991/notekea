package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A London interior designer's studio vault: three client projects, two clients named Patel.
 * Written blind on 2026-11-17; never tuned on (docs/eval-blind.md).
 */
internal object BlindInteriorDesignCases {
    const val VAULT = "Blind Interior Design"

    val all: List<AgentWideCase> = BlindInteriorDesignAnswers.all + BlindInteriorDesignChanges.all +
        BlindInteriorDesignAsks.all
}

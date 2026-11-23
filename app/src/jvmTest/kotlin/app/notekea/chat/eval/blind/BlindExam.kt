package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * A blind exam (`docs/eval-blind.md`): a vault of one invented person and its tasks. A sealed one waits for the final
 * number.
 */
internal class BlindExam(
    val id: String,
    val vault: String,
    val cases: List<AgentWideCase>,
    val isSealed: Boolean = false
)

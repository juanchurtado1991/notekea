package app.notekea.chat.eval

/**
 * Held-out tasks of the wider evaluation, over the newer notes of the `Agent Eval` vault: never
 * tuned on, they measure whether what was tuned on [AgentWideCases] generalizes. A third are in
 * Spanish.
 */
internal object AgentWideHeldOutCases {
    val all: List<AgentWideCase> = with(AgentWideHeldOutDocuments) {
        lease + handbook + audit
    } +
        with(AgentWideHeldOutLongNotes) {
            course + journal + minutes
        } +
        with(AgentWideHeldOutSeveral) {
            ambiguous + flat + water + people + clear
        }
}

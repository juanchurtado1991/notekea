package app.notekea.chat.domain.agent

import app.notekea.core.notes.domain.RelativePath

/**
 * What a question back can be remembered as (`docs/agent-memory.md`, "How a line is written"): the task's words
 * that meant any of [notes] ([said], "Laura"), so the answer chosen becomes `"Laura" is [[Laura Castro]]`, built by
 * the app and never by the model.
 */
data class Remembering(
    val said: String,
    val notes: List<RelativePath>
) {
    /** The memory's line for the [option] the person chose, when it names one of [notes]; else null. */
    fun lineFor(option: String): String? {
        val chosen = option
            .trim()
            .lowercase()

        val note = notes.firstOrNull {
            val path = it.bare()
            chosen == path.lowercase() || chosen == it.nameWithoutExtension.lowercase()
        } ?: notes.firstOrNull {
            it.nameWithoutExtension.lowercase() in chosen
        } ?: return null

        val isShared = notes.count {
            it.nameWithoutExtension == note.nameWithoutExtension
        } > 1

        val link = if (isShared) {
            note.bare()
        } else {
            note.nameWithoutExtension
        }

        return "\"$said\" is [[$link]]"
    }
}

package app.notekea.chat.domain.plan

import app.notekea.core.notes.domain.RelativePath

/** A note a step will write: where ([path]) and its whole [text]. */
data class NoteDraft(
    val path: RelativePath,
    val text: String
)

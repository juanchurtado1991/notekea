package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.RelativePath

/** A note a task can mean, and the entries inside it the task can mean ([entries] empty: the whole note). */
data class FrameTarget(
    val note: RelativePath,
    val entries: List<String> = emptyList()
)

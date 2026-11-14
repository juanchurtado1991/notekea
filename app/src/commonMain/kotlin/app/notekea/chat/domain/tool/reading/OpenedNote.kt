package app.notekea.chat.domain.tool.reading

import app.notekea.chat.domain.plan.ReadNote
import app.notekea.core.notes.domain.RelativePath

/** A note a tool opened by its name: its [path], its name as [shown], and its text with what it attaches. */
internal data class OpenedNote(
    val path: RelativePath,
    val shown: String,
    val read: ReadNote
)

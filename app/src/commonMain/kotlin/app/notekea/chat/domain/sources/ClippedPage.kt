package app.notekea.chat.domain.sources

import app.notekea.core.notes.domain.RelativePath

/** A page just clipped into the vault to answer a question: the note it became ([path]) and the [text] it holds. */
data class ClippedPage(
    val path: RelativePath,
    val text: String
)

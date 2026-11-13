package app.notekea.core.notes.domain.index

import app.notekea.core.notes.domain.RelativePath

/** A note related to the open one by meaning: its [path], how close it is ([score]), its [snippet]. */
data class RelatedNote(
    val path: RelativePath,
    val score: Float,
    val snippet: PassageSnippet
)

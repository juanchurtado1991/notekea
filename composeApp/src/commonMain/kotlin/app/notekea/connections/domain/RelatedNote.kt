package app.notekea.connections.domain

import app.notekea.core.notes.domain.PassageSnippet
import app.notekea.core.notes.domain.RelativePath

/** A note related to the open one by meaning (`ROADMAP.md` 2.4): its [path], how close it is ([score]), its [snippet]. */
data class RelatedNote(
    val path: RelativePath,
    val score: Float,
    val snippet: PassageSnippet
)

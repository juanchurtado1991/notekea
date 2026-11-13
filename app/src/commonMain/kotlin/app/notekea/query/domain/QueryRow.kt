package app.notekea.query.domain

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty

/** A note a query found: its [path] and the [cells] of the result's columns, null where it has no such property. */
data class QueryRow(
    val path: RelativePath,
    val cells: List<NoteProperty?>
)

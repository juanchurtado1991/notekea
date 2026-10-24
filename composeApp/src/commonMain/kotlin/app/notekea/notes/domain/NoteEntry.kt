package app.notekea.notes.domain

/** A note of the vault: its file and what its frontmatter says. */
data class NoteEntry(
    val file: NoteFile,
    val metadata: NoteMetadata
)

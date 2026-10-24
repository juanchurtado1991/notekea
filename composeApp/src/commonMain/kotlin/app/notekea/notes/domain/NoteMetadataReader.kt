package app.notekea.notes.domain

/** Reads what a note's frontmatter says, without reading the rest of the note. */
interface NoteMetadataReader {
    /** The note's metadata ([NoteMetadata.NONE] without frontmatter), or null when the file cannot be read now. */
    suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): NoteMetadata?
}

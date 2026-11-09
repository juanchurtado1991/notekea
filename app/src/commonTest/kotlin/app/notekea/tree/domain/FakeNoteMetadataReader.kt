package app.notekea.tree.domain

import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.RelativePath

/** Answers [metadata] per path ([NoteMetadata.NONE] by default) and records every read. */
class FakeNoteMetadataReader : NoteMetadataReader {
    val metadata = mutableMapOf<RelativePath, NoteMetadata?>()
    val reads = mutableListOf<RelativePath>()

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): NoteMetadata? {
        reads += path

        return if (path in metadata) metadata[path] else NoteMetadata.NONE
    }
}

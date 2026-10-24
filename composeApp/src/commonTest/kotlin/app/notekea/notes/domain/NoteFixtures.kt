package app.notekea.notes.domain

import kotlin.time.Instant

/** Sample notes for the notes domain tests. */
object NoteFixtures {
    const val VAULT = "/home/ana/Notes"
    private const val BASE_MILLIS = 1_800_000_000_000L
    private const val SIZE = 100L

    fun file(
        path: String,
        modifiedOffsetMillis: Long = 0L,
        size: Long? = SIZE
    ): NoteFile = NoteFile(
        path = RelativePath(value = path),
        size = size,
        modifiedAt = Instant.fromEpochMilliseconds(epochMilliseconds = BASE_MILLIS + modifiedOffsetMillis)
    )

    fun entry(
        path: String,
        modifiedOffsetMillis: Long = 0L,
        metadata: NoteMetadata = NoteMetadata.NONE
    ): NoteEntry = NoteEntry(file = file(path = path, modifiedOffsetMillis = modifiedOffsetMillis), metadata = metadata)

    fun tags(
        vararg values: String
    ): NoteMetadata = NoteMetadata.NONE.copy(tags = values.toList())
}

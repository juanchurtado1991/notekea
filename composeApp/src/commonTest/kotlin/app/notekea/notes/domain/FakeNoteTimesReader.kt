package app.notekea.notes.domain

/** Answers [times] per path, and no times for any other, as a file system that keeps none. */
class FakeNoteTimesReader : NoteTimesReader {
    val times = mutableMapOf<RelativePath, NoteTimes>()

    override suspend fun times(
        vaultPath: String,
        path: RelativePath
    ): NoteTimes = times[path] ?: NoteTimes(createdAt = null, modifiedAt = null)
}

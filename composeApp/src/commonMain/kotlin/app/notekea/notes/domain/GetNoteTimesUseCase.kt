package app.notekea.notes.domain

/** When the note was created and last modified, for the side panel's Note info. */
class GetNoteTimesUseCase(private val noteTimesReader: NoteTimesReader) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath
    ): NoteTimes = noteTimesReader.times(vaultPath = vaultPath, path = path)
}

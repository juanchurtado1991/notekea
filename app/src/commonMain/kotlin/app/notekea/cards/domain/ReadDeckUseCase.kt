package app.notekea.cards.domain

import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteEntry

/** Every card of the vault (4.6): those of its notes tagged `flashcards`, read whole so their lines are the file's. */
class ReadDeckUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): List<StudyCard> = notes
        .filter { note ->
            (note.metadata.tags + note.metadata.textTags).any { it.equals(TAG, ignoreCase = true) }
        }
        .flatMap { note ->
            val text = noteContentStore.read(
                vaultPath = vaultPath,
                path = note.file.path
            ).orEmpty()

            CardFormat.cardsOf(
                note = note.file.path,
                text = text
            )
        }

    private companion object {
        const val TAG = "flashcards"
    }
}

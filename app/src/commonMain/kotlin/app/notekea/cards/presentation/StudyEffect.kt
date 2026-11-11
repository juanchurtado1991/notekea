package app.notekea.cards.presentation

import app.notekea.core.notes.domain.RelativePath

sealed interface StudyEffect {
    data class OpenNote(
        val path: RelativePath,
        val line: Int
    ) : StudyEffect

    /** A card's schedule was written in [path]: an editor that has it open reads it again. */
    data class NoteRewritten(val path: RelativePath) : StudyEffect
}

package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Records what was revealed and answers with [outcome]. */
class FakeFileRevealer(var outcome: Outcome<Unit, NotesError> = Outcome.Success(value = Unit)) : FileRevealer {
    val revealed = mutableListOf<RelativePath>()

    override suspend fun reveal(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        revealed += path
        return outcome
    }
}

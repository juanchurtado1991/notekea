package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/*
 * New entries take the first free name, numbered as Obsidian does: `Untitled`, `Untitled 1`, `Untitled 2`… A name
 * taken between the check and the creation (another app, a sync) moves on to the next one instead of failing. The
 * candidates are bounded, so a folder that reports every name as taken cannot loop forever.
 */

private const val MAX_CANDIDATES = 1_000

suspend fun createUnderFreeName(
    noteFileOperations: NoteFileOperations,
    vaultPath: String,
    folder: RelativePath,
    baseName: String,
    extension: String,
    createAt: suspend (RelativePath) -> Outcome<Unit, NotesError>
): Outcome<RelativePath, NotesError> {
    for (number in 0 until MAX_CANDIDATES) {
        val name = if (number == 0) baseName + extension else "$baseName $number$extension"
        val path = folder.child(name = name)
        if (noteFileOperations.exists(vaultPath = vaultPath, path = path)) continue
        when (val created = createAt(path)) {
            is Outcome.Success -> return Outcome.Success(value = path)
            is Outcome.Failure -> if (created.error !is NotesError.NameTaken) return created
        }
    }
    return Outcome.Failure(error = NotesError.NameTaken(name = baseName + extension))
}

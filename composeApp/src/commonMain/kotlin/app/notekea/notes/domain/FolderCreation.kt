package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/*
 * Folders made on the way to a new entry (a daily note in `Diary/2026/October`, the templates folder): each missing
 * one is created from the top down; one made meanwhile by another app is fine.
 */

/** Creates [folder] and the folders above it that are missing; a failure, or null when they all exist. */
suspend fun createMissingFolders(
    noteFileOperations: NoteFileOperations,
    vaultPath: String,
    folder: RelativePath
): Outcome.Failure<NotesError>? {
    val missing = generateSequence(seed = folder) { it.parent }
        .filterNot { it.isRoot }
        .toList()
        .reversed()
        .filterNot { noteFileOperations.exists(vaultPath = vaultPath, path = it) }
    missing.forEach { path ->
        val created = noteFileOperations.createFolder(vaultPath = vaultPath, path = path)
        if (created is Outcome.Failure && created.error !is NotesError.NameTaken) return created
    }
    return null
}

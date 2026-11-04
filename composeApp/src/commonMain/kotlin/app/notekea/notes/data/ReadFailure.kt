package app.notekea.notes.data

import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import okio.FileSystem
import okio.IOException
import okio.Path

/**
 * Why reading [file] (the vault's [path]) failed: [NotesError.EntryMissing] only when it is really gone. The exception
 * cannot tell: the JVM reports a file without read permission as not found too.
 */
internal fun FileSystem.readFailureOf(
    file: Path,
    path: RelativePath
): NotesError = try {
    if (exists(path = file)) NotesError.Unreadable(path = path.value) else NotesError.EntryMissing(path = path.value)
} catch (_: IOException) {
    NotesError.Unreadable(path = path.value)
}

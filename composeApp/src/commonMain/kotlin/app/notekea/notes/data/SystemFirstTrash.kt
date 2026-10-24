package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.Trash
import app.notekea.notes.domain.TrashLocation
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

/**
 * [Trash] that prefers the operating system's trash, where users look first, and falls back to [vaultTrash] when the
 * system trash is not available or refuses the entry (a vault on another disk, a desktop without a trash service).
 * [moveToSystemTrash] takes an absolute path and returns whether the entry is now in the system trash.
 */
class SystemFirstTrash(
    private val moveToSystemTrash: (String) -> Boolean,
    private val vaultTrash: Trash,
    private val dispatchers: AppDispatchers
) : Trash {
    override suspend fun moveToTrash(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError> {
        val absolutePath = (vaultPath.toPath() / path.value).toString()
        val isInSystemTrash = withContext(context = dispatchers.io) { moveToSystemTrash(absolutePath) }
        return if (isInSystemTrash) {
            Outcome.Success(value = TrashLocation.SYSTEM_TRASH)
        } else {
            vaultTrash.moveToTrash(vaultPath = vaultPath, path = path)
        }
    }
}

package app.notekea.notes.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

/**
 * Watches the one note open in the editor and reports when its file changes or disappears (`ROADMAP.md` 1.5: external
 * changes are detected). It checks the file's size and date every [POLL_INTERVAL] and reads the text only when they
 * change: one cheap check per second for a single file, reliable on every system, while the vault watcher keeps
 * feeding the tree. The first report is the first change after collection starts.
 */
class ObserveNoteOnDiskUseCase(
    private val noteScanner: NoteScanner,
    private val noteContentStore: NoteContentStore
) {
    operator fun invoke(
        vaultPath: String,
        path: RelativePath
    ): Flow<NoteDiskChange> = flow {
        var known = noteScanner.stat(vaultPath = vaultPath, path = path)
        while (true) {
            delay(duration = POLL_INTERVAL)
            val current = noteScanner.stat(vaultPath = vaultPath, path = path)
            if (current == known) continue
            known = current
            if (current == null) {
                emit(value = NoteDiskChange.Deleted)
            } else {
                noteContentStore.read(vaultPath = vaultPath, path = path)?.let {
                    emit(value = NoteDiskChange.Changed(text = it))
                }
            }
        }
    }

    companion object {
        val POLL_INTERVAL = 1.seconds
    }
}

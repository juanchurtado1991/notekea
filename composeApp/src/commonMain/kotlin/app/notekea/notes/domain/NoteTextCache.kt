package app.notekea.notes.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The text of a vault's notes, kept in memory between searches so a keystroke reads only what changed. A note is read
 * again when its size or modification date differs from when it was cached, or when the file system cannot tell
 * either; notes no longer in the vault are dropped. Missing notes are read a batch at a time, in parallel. One cache
 * belongs to one vault (a new one is made for each search screen).
 */
class NoteTextCache(
    private val contentStore: NoteContentStore,
    private val textFolder: TextFolder
) {
    private val lock = Mutex()
    private val entries = mutableMapOf<RelativePath, Entry>()

    /** The text of each of [notes] that can be read; an unreadable note is left out. */
    suspend fun textsOf(
        vaultPath: String,
        notes: Collection<NoteEntry>
    ): Map<RelativePath, NoteText> {
        val stale = lock.withLock {
            entries.keys.retainAll(notes.mapTo(destination = HashSet()) { it.file.path })
            notes.filter { note -> entries[note.file.path]?.isFreshFor(file = note.file) != true }
        }
        stale.chunked(size = READ_BATCH).forEach { batch ->
            val read = coroutineScope {
                batch.map { note -> async { note.file to read(vaultPath = vaultPath, path = note.file.path) } }
                    .awaitAll()
            }
            lock.withLock { read.forEach { (file, text) -> entries[file.path] = Entry(file = file, text = text) } }
        }
        return lock.withLock {
            notes.mapNotNull { note -> entries[note.file.path]?.text?.let { note.file.path to it } }.toMap()
        }
    }

    private suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): NoteText? = contentStore.read(vaultPath = vaultPath, path = path)?.let { text ->
        val body = NoteDocument.fromText(text = text).body
        NoteText(body = body, folded = textFolder.fold(text = body))
    }

    /** A cached read; [text] is null when the note could not be read, so it is tried again only once it changes. */
    private class Entry(
        val file: NoteFile,
        val text: NoteText?
    ) {
        fun isFreshFor(
            file: NoteFile
        ): Boolean = file.size != null && file.modifiedAt != null && file == this.file
    }

    companion object {
        private const val READ_BATCH = 32
    }
}

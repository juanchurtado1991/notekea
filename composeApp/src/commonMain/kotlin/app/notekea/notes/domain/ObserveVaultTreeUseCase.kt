package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration.Companion.milliseconds

/**
 * The notes, other files and folders of a vault, kept up to date while collected. What the cache remembers is shown at once, then
 * the vault is scanned and only notes whose size or date changed are read again; every update is saved back to the
 * cache. The watcher starts before the first scan, so changes made while scanning are not lost; notes are read at most
 * [MAX_PARALLEL_READS] at a time. Changes are applied in batches gathered for [BATCH_WINDOW], so a burst (a sync, a
 * `git checkout`) produces one update instead of hundreds. A new folder or lost events cause a rescan, which reads
 * again only the notes whose size or date changed. A scan that fails emits the error and ends the flow; collecting
 * again retries.
 */
class ObserveVaultTreeUseCase(
    private val noteScanner: NoteScanner,
    private val noteMetadataReader: NoteMetadataReader,
    private val fileWatcher: FileWatcher,
    private val noteMetadataCache: NoteMetadataCache
) {
    operator fun invoke(
        vaultPath: String
    ): Flow<Outcome<VaultTree, NotesError>> = channelFlow {
        val changes = Channel<VaultChange>(capacity = Channel.UNLIMITED)
        val watching = launch { fileWatcher.watch(vaultPath = vaultPath).collect { changes.send(element = it) } }
        val cached = VaultTree.fromNotes(notes = noteMetadataCache.read(vaultPath = vaultPath))
        if (cached.notes.isNotEmpty()) send(element = Outcome.Success(value = cached))
        var saved = cached
        var tree = scanOrReport(vaultPath = vaultPath, previous = cached)
        while (tree != null) {
            send(element = Outcome.Success(value = tree))
            saveChanges(vaultPath = vaultPath, saved = saved, current = tree)
            saved = tree
            val batch = receiveBatch(changes = changes)
            tree = if (needsRescan(batch = batch)) {
                scanOrReport(vaultPath = vaultPath, previous = tree)
            } else {
                applyChanges(vaultPath = vaultPath, tree = tree, batch = batch)
            }
        }
        watching.cancel()
    }

    private suspend fun applyChanges(
        vaultPath: String,
        tree: VaultTree,
        batch: List<VaultChange>
    ): VaultTree = batch.mapNotNull { pathOf(change = it) }.distinct().fold(initial = tree) { current, path ->
        val note = if (path.isMarkdown) noteScanner.stat(vaultPath = vaultPath, path = path) else null
        val attachment = if (path.isMarkdown) null else noteScanner.statAttachment(vaultPath = vaultPath, path = path)
        when {
            note != null -> current.withNote(note = entryOf(vaultPath = vaultPath, file = note))
            attachment != null -> current.withAttachment(file = attachment)
            else -> current.without(path = path)
        }
    }

    private suspend fun entryOf(
        vaultPath: String,
        file: NoteFile
    ): NoteEntry {
        val metadata = noteMetadataReader.read(vaultPath = vaultPath, path = file.path)
        // A note that could not be read is kept without its size, so it is never trusted as unchanged and is read again.
        return if (metadata !=
            null
        ) {
            NoteEntry(file = file, metadata = metadata)
        } else {
            NoteEntry(file = file.copy(size = null), metadata = NoteMetadata.NONE)
        }
    }

    private fun needsRescan(
        batch: List<VaultChange>
    ): Boolean = batch.any { it == VaultChange.Overflow || (it is VaultChange.Created && it.isFolder) }

    private fun pathOf(
        change: VaultChange
    ): RelativePath? = when (change) {
        is VaultChange.Created -> change.path
        is VaultChange.Deleted -> change.path
        is VaultChange.Modified -> change.path
        VaultChange.Overflow -> null
    }

    private suspend fun readEntries(
        vaultPath: String,
        files: List<NoteFile>,
        previous: VaultTree
    ): List<NoteEntry> = coroutineScope {
        val permits = Semaphore(permits = MAX_PARALLEL_READS)
        files
            .map { file ->
                async {
                    val known = previous.notes[file.path]?.takeIf { isUnchanged(known = it.file, current = file) }
                    known ?: permits.withPermit { entryOf(vaultPath = vaultPath, file = file) }
                }
            }.awaitAll()
    }

    /** Stores in the cache only what changed since [saved], the tree the cache already holds. */
    private suspend fun saveChanges(
        vaultPath: String,
        saved: VaultTree,
        current: VaultTree
    ) {
        val upserted = current.notes.values.filter { saved.notes[it.file.path] != it }
        val removed = saved.notes.keys.filterNot { it in current.notes }
        if (upserted.isNotEmpty() || removed.isNotEmpty()) {
            noteMetadataCache.update(vaultPath = vaultPath, upserted = upserted, removed = removed)
        }
    }

    /** The first change, plus every change that arrives within [BATCH_WINDOW] after it. */
    private suspend fun receiveBatch(
        changes: Channel<VaultChange>
    ): List<VaultChange> {
        val first = changes.receive()
        delay(duration = BATCH_WINDOW)
        return listOf(first) + generateSequence { changes.tryReceive().getOrNull() }
    }

    private suspend fun ProducerScope<Outcome<VaultTree, NotesError>>.scanOrReport(
        vaultPath: String,
        previous: VaultTree
    ): VaultTree? = when (val scanned = noteScanner.scan(vaultPath = vaultPath)) {
        is Outcome.Failure -> {
            send(element = scanned)
            null
        }

        is Outcome.Success -> {
            val entries = readEntries(vaultPath = vaultPath, files = scanned.value.files, previous = previous)
            VaultTree(
                folders = scanned.value.folders,
                notes = entries.associateBy { it.file.path },
                attachments = scanned.value.attachments.associateBy { it.path }
            )
        }
    }

    private fun isUnchanged(
        known: NoteFile,
        current: NoteFile
    ): Boolean = known.size != null && known.modifiedAt != null && known == current

    companion object {
        const val MAX_PARALLEL_READS = 16
        val BATCH_WINDOW = 100.milliseconds
    }
}

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
 * The notes and folders of a vault, kept up to date while collected. The watcher starts before the first scan, so
 * changes made while scanning are not lost; the first scan reads every note's frontmatter, at most
 * [MAX_PARALLEL_READS] at a time. Changes are applied in batches gathered for [BATCH_WINDOW], so a burst (a sync, a
 * `git checkout`) produces one update instead of hundreds. A new folder or lost events cause a rescan, which reads
 * again only the notes whose size or date changed. A scan that fails emits the error and ends the flow; collecting
 * again retries.
 */
class ObserveVaultTreeUseCase(
    private val noteScanner: NoteScanner,
    private val noteMetadataReader: NoteMetadataReader,
    private val fileWatcher: FileWatcher
) {
    operator fun invoke(
        vaultPath: String
    ): Flow<Outcome<VaultTree, NotesError>> = channelFlow {
        val changes = Channel<VaultChange>(capacity = Channel.UNLIMITED)
        val watching = launch { fileWatcher.watch(vaultPath = vaultPath).collect { changes.send(element = it) } }
        var tree = scanOrReport(vaultPath = vaultPath, previous = VaultTree.EMPTY)
        while (tree != null) {
            send(element = Outcome.Success(value = tree))
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
        val file = noteScanner.stat(vaultPath = vaultPath, path = path)
        if (file ==
            null
        ) {
            current.without(path = path)
        } else {
            current.withNote(note = entryOf(vaultPath = vaultPath, file = file))
        }
    }

    private suspend fun entryOf(
        vaultPath: String,
        file: NoteFile
    ): NoteEntry = NoteEntry(
        file = file,
        metadata = noteMetadataReader.read(vaultPath = vaultPath, path = file.path) ?: NoteMetadata.NONE
    )

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
            VaultTree(folders = scanned.value.folders, notes = entries.associateBy { it.file.path })
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

package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteScanner
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultTree
import kotlinx.coroutines.CompletableDeferred
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
 * The notes, other files and folders of a vault, kept up to date while collected. What the cache remembers is shown at
 * once; then the vault is scanned, reading only notes whose size or date changed. The watcher starts before the first
 * scan; its changes are applied in batches gathered for [BATCH_WINDOW]. A failed scan ends the flow.
 */
class ObserveVaultTreeUseCase(
    private val noteScanner: NoteScanner,
    private val noteMetadataReader: NoteMetadataReader,
    private val fileWatcher: FileWatcher,
    private val noteMetadataCache: NoteMetadataCache
) {
    operator fun invoke(vaultPath: String): Flow<Outcome<VaultTree, NotesError>> = channelFlow {
        val changes = Channel<VaultChange>(capacity = Channel.UNLIMITED)

        val watching = launch {
            fileWatcher.watch(vaultPath = vaultPath).collect { changes.send(element = it) }
        }

        val cached = VaultTree.fromNotes(notes = noteMetadataCache.read(vaultPath = vaultPath))

        if (cached.notes.isNotEmpty()) {
            send(
                element = Outcome.Success(value = cached.copy(isFromCache = true))
            )
        }

        var saved = cached

        var tree = scanOrReport(
            vaultPath = vaultPath,
            previous = cached
        )

        while (tree != null) {
            send(element = Outcome.Success(value = tree))

            saveChanges(
                vaultPath = vaultPath,
                saved = saved,
                current = tree
            )

            saved = tree
            val batch = receiveBatch(changes = changes)

            tree = if (needsRescan(batch = batch)) {
                scanOrReport(
                    vaultPath = vaultPath,
                    previous = tree
                )
            } else {
                applyChanges(
                    vaultPath = vaultPath,
                    tree = tree,
                    batch = batch
                )
            }
        }

        watching.cancel()
    }

    private suspend fun applyChanges(
        vaultPath: String,
        tree: VaultTree,
        batch: List<VaultChange>
    ): VaultTree = batch
        .mapNotNull { pathOf(change = it) }
        .distinct()
        .fold(
            initial = withoutRenamedCase(
                tree = tree,
                batch = batch
            )
        ) { current, path ->
            val isOldCase = isOldCase(
                path = path,
                batch = batch
            )

            if (isOldCase) return@fold current

            val note = if (path.isMarkdown) {
                noteScanner.stat(
                    vaultPath = vaultPath,
                    path = path
                )
            } else {
                null
            }

            val attachment = if (path.isMarkdown) {
                null
            } else {
                noteScanner.statAttachment(
                    vaultPath = vaultPath,
                    path = path
                )
            }

            when {
                note != null -> current.withNote(
                    note = entryOf(
                        vaultPath = vaultPath,
                        file = note
                    )
                )

                attachment != null -> current.withAttachment(file = attachment)

                else -> current.without(path = path)
            }
        }

    private suspend fun entryOf(
        vaultPath: String,
        file: NoteFile
    ): NoteEntry {
        val metadata = noteMetadataReader.read(
            vaultPath = vaultPath,
            path = file.path
        )

        // A note that could not be read is kept without its size, so it is never trusted as unchanged and is read
        // again.
        return if (metadata != null) {
            NoteEntry(
                file = file,
                metadata = metadata
            )
        } else {
            NoteEntry(
                file = file.copy(size = null),
                metadata = NoteMetadata.NONE
            )
        }
    }

    private fun needsRescan(batch: List<VaultChange>): Boolean = batch.any {
        it == VaultChange.Overflow || (it is VaultChange.Created && it.isFolder)
    }

    // "Note.md" renamed "note.md" comes as a delete and a create; where the disk tells no case apart the old name
    // still answers, and stayed in the tree beside the new one (the code review of 2026-11-13).
    private fun isOldCase(
        path: RelativePath,
        batch: List<VaultChange>
    ): Boolean {
        val isDeleted = batch.any {
            it is VaultChange.Deleted && it.path == path
        }

        return isDeleted && batch.any { change ->
            val created = (change as? VaultChange.Created)?.path

            created != null &&
                created != path &&
                created.value.equals(other = path.value, ignoreCase = true)
        }
    }

    private fun withoutRenamedCase(
        tree: VaultTree,
        batch: List<VaultChange>
    ): VaultTree = batch
        .filterIsInstance<VaultChange.Deleted>()
        .map { it.path }
        .filter {
            isOldCase(
                path = it,
                batch = batch
            )
        }
        .fold(initial = tree) { current, path -> current.without(path = path) }

    private fun pathOf(change: VaultChange): RelativePath? = when (change) {
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

        // Only a changed note starts a coroutine to read it: a rescan of a vault that changed little stays cheap.
        files
            .map { file ->
                val known = previous.notes[file.path]?.takeIf {
                    isUnchanged(
                        known = it.file,
                        current = file
                    )
                }

                known?.let { CompletableDeferred(value = it) }
                    ?: async {
                        permits.withPermit {
                            entryOf(
                                vaultPath = vaultPath,
                                file = file
                            )
                        }
                    }
            }.awaitAll()
    }

    /** Stores in the cache only what changed since [saved], the tree the cache already holds. */
    private suspend fun saveChanges(
        vaultPath: String,
        saved: VaultTree,
        current: VaultTree
    ) {
        val upserted = current
            .notes
            .values
            .filter {
                saved.notes[it.file.path] != it
            }

        val removed = saved
            .notes
            .keys
            .filterNot {
                it in current.notes
            }

        val hasChanges = upserted.isNotEmpty() || removed.isNotEmpty()

        if (hasChanges) {
            noteMetadataCache.update(
                vaultPath = vaultPath,
                upserted = upserted,
                removed = removed
            )
        }
    }

    /** The first change, plus every change that arrives within [BATCH_WINDOW] after it. */
    private suspend fun receiveBatch(changes: Channel<VaultChange>): List<VaultChange> {
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
            val entries = readEntries(
                vaultPath = vaultPath,
                files = scanned.value.files,
                previous = previous
            )

            VaultTree(
                folders = scanned.value.folders,
                notes = entries.associateBy { it.file.path },
                attachments = scanned
                    .value
                    .attachments
                    .associateBy { it.path }
            )
        }
    }

    private fun isUnchanged(
        known: NoteFile,
        current: NoteFile
    ): Boolean =
        known.size != null &&
            known.modifiedAt != null &&
            known == current

    companion object {
        const val MAX_PARALLEL_READS = 16
        val BATCH_WINDOW = 100.milliseconds
    }
}

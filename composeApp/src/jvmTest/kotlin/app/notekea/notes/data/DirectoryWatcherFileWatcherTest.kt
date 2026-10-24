package app.notekea.notes.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Runs the real watcher on a temporary folder. Native events arrive at different speeds and are sometimes merged
 * (macOS reports a new file as modified), so each step waits for the event it needs instead of a fixed delay, and
 * the watcher is known to be ready once a probe note written in a loop is reported.
 */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class DirectoryWatcherFileWatcherTest {
    private val vault: Path = Files.createTempDirectory(TEMP_PREFIX)
    private val watcher = DirectoryWatcherFileWatcher(dispatchers = JvmAppDispatchers())

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `notes and folders that change are reported relative to the vault`() = watching { changes ->
        vault.resolve(IDEAS).createDirectories()
        awaitChange(
            changes = changes
        ) { it == VaultChange.Created(path = RelativePath(value = IDEAS), isFolder = true) }

        vault.resolve(NEW_NOTE).writeText(text = CONTENT)
        awaitChange(changes = changes) { isCreatedOrModified(change = it, path = NEW_NOTE) }

        vault.resolve(NEW_NOTE).writeText(text = CONTENT + CONTENT)
        awaitChange(changes = changes) { it == VaultChange.Modified(path = RelativePath(value = NEW_NOTE)) }

        vault.resolve(NEW_NOTE).deleteIfExists()
        awaitChange(changes = changes) { it == VaultChange.Deleted(path = RelativePath(value = NEW_NOTE)) }
    }

    @Test
    fun `hidden entries and files that are not notes are not reported`() = watching { changes ->
        vault.resolve(OBSIDIAN).createDirectories()
        vault.resolve("$OBSIDIAN/workspace.md").writeText(text = CONTENT)
        vault.resolve(".draft.md").writeText(text = CONTENT)
        vault.resolve("image.png").writeText(text = CONTENT)
        vault.resolve(SENTINEL).writeText(text = CONTENT)

        val beforeSentinel = mutableListOf<VaultChange>()
        awaitChange(changes = changes) { change ->
            isCreatedOrModified(change = change, path = SENTINEL).also { if (!it) beforeSentinel += change }
        }

        assertEquals(
            expected = emptyList(),
            actual = beforeSentinel.filterNot { isCreatedOrModified(change = it, path = PROBE) }
        )
    }

    @Test
    fun `a vault that cannot be watched asks for a rescan`() = runBlocking {
        val changes = Channel<VaultChange>(capacity = Channel.UNLIMITED)
        val collection =
            launch { watcher.watch(vaultPath = vault.resolve("Missing").toString()).collect { changes.send(it) } }

        assertEquals(
            expected = VaultChange.Overflow,
            actual = withTimeout(timeout = TIMEOUT) { changes.receive() }
        )
        collection.cancel()
    }

    private suspend fun awaitChange(
        changes: Channel<VaultChange>,
        matches: (VaultChange) -> Boolean
    ) {
        withTimeout(timeout = TIMEOUT) {
            while (!matches(changes.receive())) {
                // Skip the events of earlier steps until the expected one arrives.
            }
        }
    }

    private fun CoroutineScope.startProbe(): Job = launch {
        while (true) {
            vault.resolve(PROBE).writeText(text = CONTENT)
            delay(duration = PROBE_INTERVAL)
        }
    }

    private fun isCreatedOrModified(
        change: VaultChange,
        path: String
    ): Boolean = change == VaultChange.Created(path = RelativePath(value = path), isFolder = false) ||
        change == VaultChange.Modified(path = RelativePath(value = path))

    private fun watching(
        block: suspend CoroutineScope.(Channel<VaultChange>) -> Unit
    ) = runBlocking {
        val changes = Channel<VaultChange>(capacity = Channel.UNLIMITED)
        val collection = launch(context = JvmAppDispatchers().io) {
            watcher.watch(vaultPath = vault.toString()).collect { changes.send(it) }
        }
        val probe = startProbe()
        awaitChange(changes = changes) { isCreatedOrModified(change = it, path = PROBE) }
        probe.cancel()
        block(changes)
        collection.cancel()
    }

    companion object {
        private const val CONTENT = "# Note\n"
        private const val IDEAS = "Ideas"
        private const val NEW_NOTE = "Ideas/New note.md"
        private const val OBSIDIAN = ".obsidian"
        private const val PROBE = "probe.md"
        private const val SENTINEL = "sentinel.md"
        private const val TEMP_PREFIX = "notekea-watcher"
        private val PROBE_INTERVAL = 200.milliseconds
        private val TIMEOUT = 20.seconds
    }
}

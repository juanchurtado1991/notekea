package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.FileWatcher
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultChange
import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.slf4j.helpers.NOPLogger
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * [FileWatcher] on directory-watcher, which uses native events on every desktop (FSEvents on macOS, where the JDK
 * would poll). Collecting the flow starts watching and returns once every folder is registered, so no change made
 * after that is missed; cancelling it stops the watcher. Hidden entries are never reported, notes and other files
 * (images, PDFs, which the tree shows too) are, and a watcher that loses events or fails reports
 * [VaultChange.Overflow] so the vault is scanned again. A vault that is not a folder reports it at once. Native watchers say nothing when the vault
 * folder itself is moved or renamed (inotify keeps watching it at its new place), so the folder is also checked every
 * [rootCheckInterval] and its disappearance reported the same way: the rescan then finds the vault missing.
 */
class DirectoryWatcherFileWatcher(
    private val dispatchers: AppDispatchers,
    private val rootCheckInterval: Duration = ROOT_CHECK_INTERVAL
) : FileWatcher {
    override fun watch(
        vaultPath: String
    ): Flow<VaultChange> = callbackFlow {
        val root = Path(vaultPath)
        val watcher = if (root.isDirectory()) buildWatcher(root = root) { trySend(element = it) } else null
        val eventLoop = Executors.newSingleThreadExecutor { task ->
            Thread(task, THREAD_NAME).apply { isDaemon = true }
        }
        watcher?.watchAsync(eventLoop)?.whenComplete { _, error ->
            if (error !=
                null
            ) {
                trySend(element = VaultChange.Overflow)
            }
        }
            ?: trySend(element = VaultChange.Overflow)
        if (watcher != null) {
            launch {
                while (root.isDirectory()) delay(duration = rootCheckInterval)
                trySend(element = VaultChange.Overflow)
            }
        }
        awaitClose {
            watcher?.close()
            eventLoop.shutdown()
        }
    }.buffer(capacity = Channel.UNLIMITED).flowOn(context = dispatchers.io)

    private fun buildWatcher(
        root: Path,
        onChange: (VaultChange) -> Unit
    ): DirectoryWatcher? = try {
        DirectoryWatcher
            .builder()
            .path(root)
            .fileHashing(false)
            .fileTreeVisitor(HiddenSkippingFileTreeVisitor())
            .logger(NOPLogger.NOP_LOGGER)
            .listener { event -> changeOf(root = root, event = event)?.let(onChange) }
            .build()
    } catch (_: IOException) {
        null
    }

    private fun changeOf(
        root: Path,
        event: DirectoryChangeEvent
    ): VaultChange? {
        if (event.eventType() == DirectoryChangeEvent.EventType.OVERFLOW) return VaultChange.Overflow
        val path = relativePathOf(root = root, path = event.path() ?: return null) ?: return VaultChange.Overflow
        val isHidden = path.value.split(RelativePath.SEPARATOR).any { it.startsWith(prefix = HIDDEN_PREFIX) }
        return when {
            isHidden -> {
                null
            }

            event.eventType() == DirectoryChangeEvent.EventType.DELETE -> {
                VaultChange.Deleted(path = path)
            }

            event.eventType() == DirectoryChangeEvent.EventType.CREATE -> {
                VaultChange.Created(path = path, isFolder = event.isDirectory)
            }

            event.isDirectory -> {
                null
            }

            else -> {
                VaultChange.Modified(path = path)
            }
        }
    }

    /** The path inside the vault with `/` separators; null for the vault folder itself, which only a rescan can judge. */
    private fun relativePathOf(
        root: Path,
        path: Path
    ): RelativePath? {
        val relative = root.relativize(path)
        val segments = relative.map { it.toString() }.filter { it.isNotEmpty() }
        return if (segments.isEmpty()) {
            null
        } else {
            RelativePath(
                value = segments.joinToString(separator = RelativePath.SEPARATOR)
            )
        }
    }

    companion object {
        private const val HIDDEN_PREFIX = "."
        private const val THREAD_NAME = "notekea-file-watcher"
        private val ROOT_CHECK_INTERVAL = 2.seconds
    }
}

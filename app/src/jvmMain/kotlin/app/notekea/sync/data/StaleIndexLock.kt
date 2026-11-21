package app.notekea.sync.data

import co.touchlab.kermit.Logger
import org.eclipse.jgit.lib.Constants
import java.io.File
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The `.git/index.lock` a sync killed half way leaves behind: while it is there Git refuses every commit, so the vault
 * never synced nor repaired again. A lock minutes old is no one's: a sync holds it for seconds.
 */
internal object StaleIndexLock {
    private val logger = Logger.withTag(tag = "Sync")
    private val STALE_AFTER = 5.minutes
    private const val INDEX_LOCK = "index.lock"

    /** Deletes the index lock of the repository in [vault] when it is old enough to be left over. */
    fun clear(
        vault: File,
        now: Instant
    ) {
        val lock = File(vault, "${Constants.DOT_GIT}/$INDEX_LOCK")
        val age = now.toEpochMilliseconds() - lock.lastModified()
        val isStale = lock.isFile && age > STALE_AFTER.inWholeMilliseconds
        if (!isStale) return

        lock.delete()
        logger.w { "A Git lock left by an earlier sync was removed" }
    }
}

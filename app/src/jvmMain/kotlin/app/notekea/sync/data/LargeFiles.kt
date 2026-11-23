package app.notekea.sync.data

import co.touchlab.kermit.Logger
import org.eclipse.jgit.api.Git
import java.io.File

/**
 * Files no Git host takes (GitHub refuses any over 100 MB): one is left out of the repository, in its own exclude
 * file, before it is committed; once committed, every push was refused (the code review of 2026-11-13). It stays in
 * the vault, only not synced.
 */
internal object LargeFiles {
    const val MAX_BYTES = 100L * 1024 * 1024
    private val logger = Logger.withTag(tag = "Sync")

    /**
     * Leaves out of [git] the files among [changed], new or already in it, that are over
     * [maxBytes]; their paths. The code review of 2026-11-22: only new files were measured, so one
     * already synced that grew past the limit was committed, and every later push refused.
     */
    fun leaveOut(
        git: Git,
        changed: Set<String>,
        maxBytes: Long = MAX_BYTES
    ): List<String> {
        val workTree = git.repository.workTree

        val large = changed.filter {
            File(workTree, it).length() > maxBytes
        }

        if (large.isEmpty()) return emptyList()

        untrack(
            git = git,
            paths = large
        )

        JGitExclude.add(
            git = git,
            paths = large
        )

        logger.w { "${large.size} file(s) over 100 MB are not synced" }

        return large
    }

    // An exclude never untracks a file: one the repository already holds leaves its index, so
    // this commit drops it there and the other devices stop getting it.
    private fun untrack(
        git: Git,
        paths: List<String>
    ) {
        val index = git.repository.readDirCache()

        val tracked = paths.filter {
            index.getEntry(it) != null
        }

        if (tracked.isEmpty()) return

        val remove = git.rm().setCached(true)

        tracked.forEach { remove.addFilepattern(it) }

        remove.call()
    }
}

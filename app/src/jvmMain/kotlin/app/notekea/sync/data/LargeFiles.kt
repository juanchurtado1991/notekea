package app.notekea.sync.data

import co.touchlab.kermit.Logger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File

/**
 * Files no Git host takes (GitHub refuses any over 100 MB): one is left out of the repository, in its own exclude
 * file, before it is committed; once committed, every push was refused (the code review of 2026-11-13). It stays in
 * the vault, only not synced, and waits in [LeftOutNotice] for the person to be told.
 */
internal object LargeFiles {
    const val MAX_BYTES = 100L * 1024 * 1024
    private const val REMOTE_REFS = "refs/remotes/origin/"
    private val logger = Logger.withTag(tag = "Sync")

    /**
     * Leaves out of [git] the files among [changed], new or already in it, that are over [maxBytes]; their paths. One
     * the remote already holds that large stays: its host takes it, and leaving it out would delete it everywhere.
     */
    fun leaveOut(
        git: Git,
        changed: Set<String>,
        maxBytes: Long = MAX_BYTES
    ): List<String> {
        val workTree = git.repository.workTree

        val over = changed.filter {
            File(workTree, it).length() > maxBytes
        }

        if (over.isEmpty()) return emptyList()

        val taken = takenByRemote(
            git = git,
            paths = over,
            maxBytes = maxBytes
        )

        val large = over - taken
        if (large.isEmpty()) return emptyList()

        untrack(
            git = git,
            paths = large
        )

        JGitExclude.add(
            git = git,
            paths = large
        )

        LeftOutNotice.add(
            gitDir = git.repository.directory,
            paths = large
        )

        logger.w { "Left out of sync, over the size a Git host takes: ${large.size} file(s)" }

        return large
    }

    // An exclude never untracks a file: one the repository already holds leaves its index, so this commit drops it
    // there and the other devices stop getting it.
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

    // The paths the remote's branch, as last fetched, holds over the limit: a host that took them takes them again.
    private fun takenByRemote(
        git: Git,
        paths: List<String>,
        maxBytes: Long
    ): Set<String> {
        val repository = git.repository

        val tree =
            repository.resolve("$REMOTE_REFS${repository.branch}^{tree}") ?: return emptySet()

        return repository.newObjectReader().use { reader ->
            paths
                .filterTo(destination = mutableSetOf()) { path ->
                    TreeWalk
                        .forPath(reader, path, tree)
                        ?.use {
                            reader.getObjectSize(it.getObjectId(0), Constants.OBJ_BLOB) > maxBytes
                        } == true
                }
        }
    }
}

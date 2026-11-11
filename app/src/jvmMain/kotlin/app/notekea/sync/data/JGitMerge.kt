package app.notekea.sync.data

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictCopies
import app.notekea.sync.domain.PullResult
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeResult
import org.eclipse.jgit.api.RebaseCommand
import org.eclipse.jgit.api.RebaseResult
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File

/**
 * Brings the remote's commit [theirs] into the vault: a rebase of ours on it when nothing clashes, so the history
 * stays a line; when a note changed on both sides, a merge that keeps the remote's version in the note and ours in
 * its conflict copy, both committed, so a pull never stops half done.
 */
internal class JGitMerge(
    private val git: Git,
    private val commit: (message: String) -> Unit
) {
    private val repository: Repository = git.repository

    fun bring(theirs: ObjectId): PullResult {
        val before = repository.resolve(HEAD)
        if (before == null) return fastForward(theirs = theirs)

        val rebase = git
            .rebase()
            .setUpstream(theirs)
            .call()

        if (rebase.status.isSuccessful) {
            return PullResult(
                changed = changedBetween(before),
                conflicts = emptyList()
            )
        }

        if (repository.repositoryState.isRebasing) {
            git
                .rebase()
                .setOperation(RebaseCommand.Operation.ABORT)
                .call()
        }

        return merged(
            before = before,
            theirs = theirs
        )
    }

    // A vault with nothing committed yet (an empty folder): its branch starts at the remote's commit.
    private fun fastForward(theirs: ObjectId): PullResult {
        repository.updateRef(repository.fullBranch).apply {
            setNewObjectId(theirs)
            forceUpdate()
        }

        git
            .reset()
            .setRef(HEAD)
            .setMode(ResetCommand.ResetType.HARD)
            .call()

        return PullResult(
            changed = changedBetween(before = null),
            conflicts = emptyList()
        )
    }

    private fun merged(
        before: ObjectId,
        theirs: ObjectId
    ): PullResult {
        val result = git
            .merge()
            .include(theirs)
            .setCommit(true)
            .setMessage(MERGE_MESSAGE)
            .call()

        val clashing = result
            .conflicts
            ?.keys
            .orEmpty()

        if (result.mergeStatus != MergeResult.MergeStatus.CONFLICTING) {
            return PullResult(
                changed = changedBetween(before),
                conflicts = emptyList()
            )
        }

        val kept = clashing.mapNotNull { path ->
            keepBoth(
                path = path,
                ours = blobOf(
                    commit = before,
                    path = path
                ),
                theirs = blobOf(
                    commit = theirs,
                    path = path
                )
            )
        }

        git
            .add()
            .addFilepattern(ALL)
            .call()

        git
            .add()
            .setUpdate(true)
            .addFilepattern(ALL)
            .call()

        commit("$MERGE_MESSAGE, both versions kept of ${clashing.size}")

        return PullResult(
            changed = changedBetween(before),
            conflicts = kept
        )
    }

    /** The remote's [theirs] in the note and our [ours] beside it; the note, when both still have it. */
    private fun keepBoth(
        path: String,
        ours: ByteArray?,
        theirs: ByteArray?
    ): RelativePath? {
        val file = File(repository.workTree, path)
        val note = RelativePath(value = path)

        if (theirs == null || ours == null) {
            (theirs ?: ours)?.let { file.writeBytes(it) } ?: file.delete()

            return null
        }

        file.writeBytes(theirs)
        File(repository.workTree, ConflictCopies.copyOf(path = note).value).writeBytes(ours)

        return note
    }

    private fun blobOf(
        commit: ObjectId,
        path: String
    ): ByteArray? = RevWalk(repository).use { walk ->
        val tree = walk.parseCommit(commit).tree

        TreeWalk
            .forPath(repository, path, tree)
            ?.use { repository.open(it.getObjectId(0)).bytes }
    }

    private fun changedBetween(before: ObjectId?): Int {
        val after = repository.resolve(HEAD) ?: return 0

        return repository.newObjectReader().use { reader ->
            val old = CanonicalTreeParser()

            before?.let {
                old.reset(reader, RevWalk(repository).use { walk -> walk.parseCommit(it).tree })
            }

            val new = CanonicalTreeParser().apply {
                reset(reader, RevWalk(repository).use { walk -> walk.parseCommit(after).tree })
            }

            git
                .diff()
                .setOldTree(old)
                .setNewTree(new)
                .call()
                .count {
                    it.changeType != DiffEntry.ChangeType.COPY
                }
        }
    }

    private val RebaseResult.Status.isSuccessful: Boolean
        get() =
            this == RebaseResult.Status.OK ||
                this == RebaseResult.Status.UP_TO_DATE ||
                this == RebaseResult.Status.FAST_FORWARD

    companion object {
        const val HEAD = "HEAD"
        const val ALL = "."
        const val MERGE_MESSAGE = "Notekea: sync"
    }
}

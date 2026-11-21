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

        val isConflicting = result.mergeStatus == MergeResult.MergeStatus.CONFLICTING

        if (!isConflicting) {
            // A merge that did not happen (a note changed meanwhile, a file Git could not write) is no pull: said
            // with the files it stopped at, rather than the push being refused next with no cause shown.
            check(result.mergeStatus.isSuccessful) { notMerged(result = result) }

            return PullResult(
                changed = changedBetween(before),
                conflicts = emptyList()
            )
        }

        refuseFileAgainstFolder(
            clashing = clashing,
            before = before
        )

        // A step that fails here would leave the vault half merged, its notes full of conflict marks, and the
        // next sync would commit them: everything goes back to what was committed before the merge instead.
        val kept = try {
            bothKept(
                clashing = clashing,
                before = before,
                theirs = theirs
            )
        } catch (exception: Exception) {
            git
                .reset()
                .setMode(ResetCommand.ResetType.HARD)
                .setRef(before.name)
                .call()

            throw exception
        }

        return PullResult(
            changed = changedBetween(before),
            conflicts = kept
        )
    }

    private fun bothKept(
        clashing: Set<String>,
        before: ObjectId,
        theirs: ObjectId
    ): List<RelativePath> {
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

        return kept
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
        val copy = File(repository.workTree, ConflictCopies.copyOf(path = note).value)

        // An older conflict of this note not settled yet: its copy is the person's too, set aside and not lost.
        if (copy.exists()) {
            val mine = File(repository.workTree, ConflictCopies.mineOf(path = note).value)

            FreeFile.moveBeside(
                file = copy,
                wanted = mine
            )
        }

        copy.writeBytes(ours)

        return note
    }

    // A note on one side where the other has a folder of that name (or a link) is left unmerged with no content
    // conflict, and a commit would keep one side only: the vault goes back to its last commit and the person renames
    // one of them.
    private fun refuseFileAgainstFolder(
        clashing: Set<String>,
        before: ObjectId
    ) {
        val unmerged = git
            .status()
            .call()
            .conflicting - clashing

        if (unmerged.isEmpty()) return

        git
            .reset()
            .setMode(ResetCommand.ResetType.HARD)
            .setRef(before.name)
            .call()

        error("$FILE_AND_FOLDER ${unmerged.sorted().joinToString()}")
    }

    private fun notMerged(result: MergeResult): String {
        val paths = result
            .failingPaths
            ?.keys
            .orEmpty() + result.checkoutConflicts.orEmpty()

        return "$NOT_MERGED (${result.mergeStatus}): ${paths.sorted().joinToString()}"
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
        const val NOT_MERGED = "The remote's changes could not be brought in"
        const val FILE_AND_FOLDER =
            "A note and a folder (or another kind of file) share a name here and on the remote, rename one:"
    }
}

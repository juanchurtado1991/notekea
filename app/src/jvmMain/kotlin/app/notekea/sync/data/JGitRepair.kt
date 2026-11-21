package app.notekea.sync.data

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.RepairChoice
import app.notekea.sync.domain.UnfinishedSync
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.RebaseCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryState
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * A vault left in the middle of a merge or a rebase, when the app was killed: its notes may carry Git's conflict
 * marks, and a sync would commit them. Nothing is repaired by itself, a hard reset would lose what was written
 * since: the person chooses, and either way what they wrote is kept somewhere.
 */
internal class JGitRepair(private val git: Git) {
    private val repository = git.repository
    private val vault: File = repository.workTree

    val isUnfinished: Boolean
        get() = repository.repositoryState != RepositoryState.SAFE &&
            repository.repositoryState != RepositoryState.BARE

    // The branch being synced. A rebase leaves HEAD detached, its name a commit's, and keeps the branch's in a file.
    private val branch: String
        get() = REBASE_HEAD_NAMES
            .map { File(repository.directory, it) }
            .firstOrNull { it.isFile }
            ?.readText()
            ?.trim()
            ?.let { Repository.shortenRefName(it) }
            ?: repository.branch

    fun unfinished(): UnfinishedSync? {
        if (!isUnfinished) return null

        // The notes Git marked as clashing are among them: they were changed on both sides.
        val changed = JGitPending.namesOf(
            git = git,
            branch = branch
        ) + git
            .status()
            .call()
            .conflicting

        return UnfinishedSync(changedHere = changed.sorted().map { RelativePath(value = it) })
    }

    fun repair(choice: RepairChoice) {
        if (!isUnfinished) return

        when (choice) {
            RepairChoice.KEEP_MINE -> keepMine()
            RepairChoice.TAKE_REMOTE -> takeRemote()
        }
    }

    // What was written here after the merge stopped (changed in the folder, not by the merge) is set aside, the
    // merge undone to the last commit made here, and those files put back. The notes Git marked as clashing go
    // back to that commit too: the next sync meets the remote's side again and keeps both, as any conflict. What
    // they held, the person's own hand on them included, is copied to the vault's trash first.
    private fun keepMine() {
        val status = git.status().call()
        val written = (status.modified - status.conflicting).filter { File(vault, it).isFile }
        val clashing = status.conflicting
        val aside = Files.createTempDirectory(ASIDE)

        if (clashing.isNotEmpty()) {
            trashed(
                paths = clashing,
                trash = freeTrashFolder()
            )
        }

        try {
            written.forEach {
                copy(
                    from = File(vault, it),
                    to = aside.resolve(it).toFile()
                )
            }

            undo()

            written.forEach {
                copy(
                    from = aside.resolve(it).toFile(),
                    to = File(vault, it)
                )
            }
        } finally {
            aside.toFile().deleteRecursively()
        }
    }

    // Everything changed here is copied to the vault's trash, then the vault is the remote's last commit. What is
    // written in the folder now goes first, as the person left it; the commits not pushed are read once the rebase
    // or the merge is undone, as a rebase cut short has not replayed them all yet.
    private fun takeRemote() {
        val trash = freeTrashFolder()
        val status = git.status().call()

        trashed(
            paths = status.modified + status.untracked + status.conflicting,
            trash = trash
        )

        undo()

        trashed(
            paths = JGitPending.namesOf(git = git),
            trash = trash
        )

        val remote =
            repository.resolve("${JGitPending.REMOTE_PREFIX}${repository.branch}") ?: return

        git
            .reset()
            .setMode(ResetCommand.ResetType.HARD)
            .setRef(remote.name)
            .call()
    }

    // The files at [paths] copied, as they are now, to [trash]. A path already there with other content is kept
    // too, beside it: the hand-merged note and the version committed here are both the person's.
    private fun trashed(
        paths: Collection<String>,
        trash: File
    ) {
        paths
            .filter { File(vault, it).isFile }
            .forEach {
                keep(
                    path = it,
                    trash = trash
                )
            }
    }

    private fun keep(
        path: String,
        trash: File
    ) {
        val file = File(vault, path)
        val wanted = File(trash, path)
        val isKept = wanted.isFile && wanted.readBytes().contentEquals(file.readBytes())
        if (isKept) return

        copy(
            from = file,
            to = FreeFile.beside(file = wanted)
        )
    }

    // "Sync repair" in the vault's trash, numbered when an earlier repair left one: never one written over.
    private fun freeTrashFolder(): File = generateSequence(seed = 1) {
        it + 1
    }
        .map { number ->
            val suffix = if (number == 1) "" else " $number"

            File(vault, "$TRASH/$REPAIR_FOLDER$suffix")
        }
        .first {
            !it.exists()
        }

    // Back to the last commit made here, out of the merge or the rebase.
    private fun undo() {
        if (repository.repositoryState.isRebasing) {
            git
                .rebase()
                .setOperation(RebaseCommand.Operation.ABORT)
                .call()
        }

        if (repository.resolve(Constants.HEAD) == null) return

        git
            .reset()
            .setMode(ResetCommand.ResetType.HARD)
            .setRef(Constants.HEAD)
            .call()
    }

    private fun copy(
        from: File,
        to: File
    ) {
        to.parentFile.mkdirs()
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private companion object {
        const val ASIDE = "notekea-repair"

        // The vault's trash as the tree keeps it, which the sync leaves out (`.git/info/exclude`).
        const val TRASH = ".trash"
        const val REPAIR_FOLDER = "Sync repair"

        // Where a rebase keeps the name of the branch it took HEAD from (JGit's merge rebase, Git's apply).
        val REBASE_HEAD_NAMES = listOf("rebase-merge/head-name", "rebase-apply/head-name")
    }
}

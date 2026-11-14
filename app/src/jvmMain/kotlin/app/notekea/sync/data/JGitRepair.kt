package app.notekea.sync.data

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.RepairChoice
import app.notekea.sync.domain.UnfinishedSync
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.RebaseCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
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

    fun unfinished(): UnfinishedSync? {
        if (!isUnfinished) return null

        // The notes Git marked as clashing are among them: they were changed on both sides.
        val changed = JGitPending.namesOf(git = git) + git
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

    private val isUnfinished: Boolean
        get() = repository.repositoryState != RepositoryState.SAFE &&
            repository.repositoryState != RepositoryState.BARE

    // What was written here after the merge stopped (changed in the folder, not by the merge) is set aside, the
    // merge undone to the last commit made here, and those files put back. The notes Git marked as clashing go
    // back to that commit too: the next sync meets the remote's side again and keeps both, as any conflict.
    private fun keepMine() {
        val status = git.status().call()
        val written = (status.modified - status.conflicting).filter { File(vault, it).isFile }
        val aside = Files.createTempDirectory(ASIDE)

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

    // Everything changed here is copied to the vault's trash, then the vault is the remote's last commit.
    private fun takeRemote() {
        val changed = unfinished()?.changedHere.orEmpty()
        val trash = freeTrashFolder()

        changed
            .map { it.value }
            .filter { File(vault, it).isFile }
            .forEach {
                copy(
                    from = File(vault, it),
                    to = File(trash, it)
                )
            }

        undo()

        val remote = repository.resolve("$REMOTE_PREFIX${repository.branch}") ?: return

        git
            .reset()
            .setMode(ResetCommand.ResetType.HARD)
            .setRef(remote.name)
            .call()
    }

    // "Sync repair" in the vault's trash, numbered when an earlier repair left one: never one written over.
    private fun freeTrashFolder(): File = generateSequence(seed = 1) {
        it + 1
    }
        .map { number -> File(vault, "$TRASH/$TRASH_FOLDER" + if (number == 1) "" else " $number") }
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
        const val REMOTE_PREFIX = "refs/remotes/origin/"
        const val ASIDE = "notekea-repair"
        const val TRASH = ".trash"
        const val TRASH_FOLDER = "Sync repair"
    }
}

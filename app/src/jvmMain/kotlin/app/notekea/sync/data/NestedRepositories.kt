package app.notekea.sync.data

import co.touchlab.kermit.Logger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import java.io.File

/**
 * Folders of the vault that hold a Git repository of their own (a project cloned into it). Git would commit one as
 * a gitlink, a pointer to a commit no other device has, and its notes would never reach them: a new one is left out
 * of the vault's repository instead, and stays as it is on this computer.
 */
internal object NestedRepositories {
    private val logger = Logger.withTag(tag = "Sync")

    /** Leaves out of [git] the folders among [untracked] that hold a `.git`; their paths. */
    fun leaveOut(
        git: Git,
        untracked: Set<String>
    ): List<String> {
        val workTree = git.repository.workTree

        // Git lists such a folder by its own path, as the one entry it would commit for it.
        val nested = untracked.filter { File(workTree, "$it/${Constants.DOT_GIT}").exists() }

        if (nested.isEmpty()) return emptyList()

        JGitExclude.add(
            git = git,
            paths = nested
        )

        logger.w { "${nested.size} folder(s) holding a Git repository of their own are not synced" }

        return nested
    }
}

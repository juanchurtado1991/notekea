package app.notekea.sync.data

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants

/**
 * The branch a vault syncs on when it is first connected: the remote's when it already has one, so a vault made in
 * Notekea (on `main`) joins a remote whose history is on `master`, or another single branch, instead of starting a
 * second one beside it.
 */
internal object JGitBranches {
    private const val REMOTE_PREFIX = "refs/remotes/origin/"
    private val PREFERRED = listOf("main", "master")

    fun followRemote(git: Git) {
        val repository = git.repository
        val local = repository.branch

        val remote = repository
            .refDatabase
            .getRefsByPrefix(REMOTE_PREFIX)
            .map { it.name.removePrefix(prefix = REMOTE_PREFIX) }
            .filter {
                it != Constants.HEAD
            }

        if (remote.isEmpty() || local in remote) return

        val chosen = PREFERRED.firstOrNull {
            it in remote
        } ?: remote.singleOrNull() ?: return

        if (repository.resolve(Constants.HEAD) == null) {
            // Nothing committed yet: the branch only has a name, which follows the remote's.
            repository.updateRef(Constants.HEAD).link("${Constants.R_HEADS}$chosen")

            return
        }

        git
            .branchRename()
            .setOldName(local)
            .setNewName(chosen)
            .call()
    }
}

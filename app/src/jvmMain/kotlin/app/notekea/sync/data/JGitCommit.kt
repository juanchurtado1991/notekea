package app.notekea.sync.data

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.EmptyCommitException
import org.eclipse.jgit.lib.RepositoryState

/**
 * Commits every change of the vault, leaving out first what no other device could use: files over the host's limit
 * ([LargeFiles]) and folders holding a repository of their own ([NestedRepositories]).
 */
internal object JGitCommit {
    private const val ALL = "."
    private const val USER = "user"
    private const val NAME = "name"
    private const val EMAIL = "email"
    private const val AUTHOR = "Notekea"
    private const val AUTHOR_EMAIL = "notekea@localhost"

    /** False when there was nothing to commit. */
    fun all(
        git: Git,
        message: String
    ): Boolean {
        val status = git.status().call()
        if (status.isClean) return false

        LargeFiles.leaveOut(
            git = git,
            untracked = status.untracked
        )

        NestedRepositories.leaveOut(
            git = git,
            untracked = status.untracked
        )

        git
            .add()
            .addFilepattern(ALL)
            .call()

        git
            .add()
            .setUpdate(true)
            .addFilepattern(ALL)
            .call()

        return commit(
            git = git,
            message = message
        )
    }

    private fun commit(
        git: Git,
        message: String
    ): Boolean {
        val config = git.repository.config
        val name = config.getString(USER, null, NAME) ?: AUTHOR
        val email = config.getString(USER, null, EMAIL) ?: AUTHOR_EMAIL

        // A status that was not clean may still leave nothing to commit (only what was just left out, or a nested
        // repository with changes of its own): no empty commit then. A merge's commit is made whatever it holds.
        val isMerging = git.repository.repositoryState != RepositoryState.SAFE

        return try {
            git
                .commit()
                .setMessage(message)
                .setAuthor(name, email)
                .setCommitter(name, email)
                .setAllowEmpty(isMerging)
                .call()

            true
        } catch (_: EmptyCommitException) {
            false
        }
    }
}

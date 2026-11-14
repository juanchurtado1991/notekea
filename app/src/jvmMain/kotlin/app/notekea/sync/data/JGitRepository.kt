package app.notekea.sync.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.ConflictCopies
import app.notekea.sync.domain.GitRepository
import app.notekea.sync.domain.PullResult
import app.notekea.sync.domain.SyncError
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * [GitRepository] with JGit on the vault's folder: one remote, `origin`, one branch; what Notekea writes for itself
 * stays out through `.git/info/exclude`, which is never committed nor touches the vault's own `.gitignore`.
 */
class JGitRepository(private val dispatchers: AppDispatchers) : GitRepository {
    override suspend fun connect(
        vaultPath: String,
        remoteUrl: String,
        token: String?
    ): Outcome<PullResult, SyncError> = git(
        vaultPath = vaultPath,
        isCreated = true
    ) { git ->
        git
            .repository
            .config
            .apply {
                setString(REMOTE, ORIGIN, URL, remoteUrl)
                setString(REMOTE, ORIGIN, FETCH, "+refs/heads/*:refs/remotes/origin/*")
                save()
            }

        commitIn(
            git = git,
            message = FIRST_MESSAGE
        )

        val pulled = pullIn(
            git = git,
            token = token,
            isFirst = true
        )

        pushIn(
            git = git,
            token = token
        )

        pulled
    }

    override suspend fun commitAll(
        vaultPath: String,
        message: String
    ): Outcome<Boolean, SyncError> = git(vaultPath = vaultPath) {
        commitIn(
            git = it,
            message = message
        )
    }

    override suspend fun pull(
        vaultPath: String,
        token: String?
    ): Outcome<PullResult, SyncError> = git(vaultPath = vaultPath) {
        pullIn(
            git = it,
            token = token,
            isFirst = false
        )
    }

    override suspend fun push(
        vaultPath: String,
        token: String?
    ): Outcome<Unit, SyncError> = git(vaultPath = vaultPath) {
        pushIn(
            git = it,
            token = token
        )
    }

    override suspend fun pendingChanges(vaultPath: String): Int =
        (git(vaultPath = vaultPath) { JGitPending.of(git = it) } as? Outcome.Success)?.value ?: 0

    override suspend fun conflicts(vaultPath: String): List<RelativePath> =
        withContext(context = dispatchers.io) {
            val root = File(vaultPath)
            if (!root.isDirectory) return@withContext emptyList()

            // Never into .git, whose objects outnumber the notes; this runs after every sync.
            root
                .walkTopDown()
                .onEnter {
                    it.name != GIT_FOLDER
                }
                .filter { it.isFile }
                .map { RelativePath(value = it.relativeTo(root).invariantSeparatorsPath) }
                .mapNotNull { ConflictCopies.noteOf(copy = it) }
                .filter { File(vaultPath, it.value).isFile }
                .sortedBy { it.value }
                .toList()
        }

    override suspend fun resolve(
        vaultPath: String,
        note: RelativePath,
        choice: ConflictChoice
    ): Outcome<Unit, SyncError> = git(vaultPath = vaultPath) { git ->
        val copy = File(vaultPath, ConflictCopies.copyOf(path = note).value).toPath()
        val original = File(vaultPath, note.value).toPath()

        when (choice) {
            ConflictChoice.MINE -> Files.move(copy, original, StandardCopyOption.REPLACE_EXISTING)

            ConflictChoice.THEIRS -> Files.delete(copy)

            ConflictChoice.BOTH -> {
                // "Note (mine).md" may be there from an earlier conflict: a free name, never a failure.
                val wanted = File(vaultPath, ConflictCopies.mineOf(path = note).value)
                Files.move(copy, FreeFile.beside(file = wanted).toPath())
            }
        }

        commitIn(
            git = git,
            message = "$RESOLVED_MESSAGE ${note.nameWithoutExtension}"
        ).let { }
    }

    private suspend fun <T> git(
        vaultPath: String,
        isCreated: Boolean = false,
        block: (Git) -> T
    ): Outcome<T, SyncError> = withContext(context = dispatchers.io) {
        try {
            val folder = File(vaultPath)
            val isRepository = File(folder, Constants.DOT_GIT).isDirectory

            val git = when {
                isRepository -> Git.open(folder)

                isCreated ->
                    Git
                        .init()
                        .setDirectory(folder)
                        .setInitialBranch(MAIN)
                        .call()

                else -> return@withContext Outcome.Failure(error = NOT_A_REPOSITORY)
            }

            git.use {
                excludeOwnFiles(git = it)
                keepLineEnds(git = it)
                Outcome.Success(value = block(it))
            }
        } catch (exception: Exception) {
            if (exception is kotlinx.coroutines.CancellationException) throw exception
            Outcome.Failure(error = JGitErrors.of(throwable = exception))
        }
    }

    private fun commitIn(
        git: Git,
        message: String
    ): Boolean {
        if (git.status().call().isClean) return false
        LargeFiles.leaveOut(git = git)

        git
            .add()
            .addFilepattern(ALL)
            .call()

        git
            .add()
            .setUpdate(true)
            .addFilepattern(ALL)
            .call()

        val name = git
            .repository
            .config
            .getString(USER, null, NAME) ?: AUTHOR

        val email = git
            .repository
            .config
            .getString(USER, null, EMAIL) ?: AUTHOR_EMAIL

        git
            .commit()
            .setMessage(message)
            .setAuthor(name, email)
            .setCommitter(name, email)
            .call()

        return true
    }

    private fun pullIn(
        git: Git,
        isFirst: Boolean,
        token: String?
    ): PullResult {
        git
            .fetch()
            .setRemote(ORIGIN)
            // A network that stopped answering ends the sync with an error, never a ring that turns for good.
            .setTimeout(NETWORK_SECONDS)
            .setCredentialsProvider(credentialsOf(token = token))
            .call()

        if (isFirst) JGitBranches.followRemote(git = git)

        val theirs: ObjectId = git.repository.resolve("$REMOTE_REFS${git.repository.branch}")
            ?: return PullResult(
                changed = 0,
                conflicts = emptyList()
            )

        return JGitMerge(git = git) {
            commitIn(
                git = git,
                message = it
            )
        }.bring(theirs = theirs)
    }

    private fun pushIn(
        git: Git,
        token: String?
    ) {
        val head = git.repository.resolve(JGitMerge.HEAD) ?: return
        val branch = git.repository.branch

        // With nothing the remote lacks there is nothing to push, and the remote is not asked to be written: a
        // repository the person can only read (someone else's notes, a read-only token) still connects and syncs
        // what it publishes (code review, 2026-11-14: importing one failed after its notes were brought).
        val isUpToDate = head == git.repository.resolve("refs/remotes/$ORIGIN/$branch")

        if (isUpToDate) {
            return track(
                git = git,
                branch = branch
            )
        }

        val updates = git
            .push()
            .setRemote(ORIGIN)
            .setTimeout(NETWORK_SECONDS)
            .setRefSpecs(RefSpec("refs/heads/$branch:refs/heads/$branch"))
            .setCredentialsProvider(credentialsOf(token = token))
            .call()
            .flatMap { it.remoteUpdates }

        val refused = updates.firstOrNull {
            it.status != RemoteRefUpdate.Status.OK && it.status != RemoteRefUpdate.Status.UP_TO_DATE
        }

        if (refused != null) error("$PUSH_REFUSED ${refused.status}: ${refused.message.orEmpty()}")

        track(
            git = git,
            branch = branch
        )
    }

    private fun track(
        git: Git,
        branch: String
    ) {
        git
            .repository
            .config
            .apply {
                setString(BRANCH, branch, REMOTE, ORIGIN)
                setString(BRANCH, branch, MERGE, "refs/heads/$branch")
                save()
            }
    }

    private fun credentialsOf(token: String?): CredentialsProvider? = token?.takeIf {
        it.isNotBlank()
    }?.let { UsernamePasswordCredentialsProvider(TOKEN_USER, it) }

    // Notes stay byte for byte as written: Git for Windows sets core.autocrlf, which would rewrite their line ends.
    private fun keepLineEnds(git: Git) {
        val config = git.repository.config
        val isKept = config.getString(CORE, null, AUTO_CRLF) == FALSE
        if (isKept) return

        config.setString(CORE, null, AUTO_CRLF, FALSE)
        config.save()
    }

    // Notekea's own files: a half-written save, its trash, Obsidian's window layout, macOS's folder files.
    private fun excludeOwnFiles(git: Git) {
        val exclude = File(git.repository.directory, "info/exclude")
        val lines = if (exclude.isFile) exclude.readLines() else emptyList()

        val missing = EXCLUDED.filterNot {
            it in lines
        }

        if (missing.isEmpty()) return

        exclude.parentFile.mkdirs()

        exclude.appendText(
            missing.joinToString(
                separator = "\n",
                prefix = "\n",
                postfix = "\n"
            )
        )
    }

    private companion object {
        // As long as a slow connection may stay silent in the middle of a fetch or a push.
        const val NETWORK_SECONDS = 60
        const val MAIN = "main"
        const val CORE = "core"
        const val AUTO_CRLF = "autocrlf"
        const val FALSE = "false"
        const val ORIGIN = "origin"
        const val REMOTE = "remote"
        const val BRANCH = "branch"
        const val MERGE = "merge"
        const val URL = "url"
        const val FETCH = "fetch"
        const val USER = "user"
        const val NAME = "name"
        const val EMAIL = "email"
        const val ALL = "."
        const val GIT_FOLDER = ".git"
        const val REMOTE_REFS = "refs/remotes/origin/"
        const val AUTHOR = "Notekea"
        const val AUTHOR_EMAIL = "notekea@localhost"
        const val TOKEN_USER = "x-access-token"
        const val FIRST_MESSAGE = "Notekea: the vault before its first sync"
        const val RESOLVED_MESSAGE = "Notekea: settled the conflict of"
        const val PUSH_REFUSED = "The remote refused the push"
        val NOT_A_REPOSITORY = SyncError.Failed(message = "This vault is not a Git repository yet")
        val EXCLUDED =
            listOf("*.notekea-tmp", ".trash/", ".obsidian/workspace.json", ".obsidian/workspace-mobile.json", ".DS_Store")
    }
}

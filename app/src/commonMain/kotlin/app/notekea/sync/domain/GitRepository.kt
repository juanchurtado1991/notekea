package app.notekea.sync.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath

/**
 * A vault as a Git repository (JGit on desktop). A conflict never stops a pull: the note keeps the remote's version
 * and ours is written beside it as "Note (conflict).md" (`ConflictCopies`), both committed. [token] is null for a
 * remote that needs none (a local one).
 */
interface GitRepository {
    /** Makes the vault a repository of [remoteUrl] (a new one, or an existing one with that remote) and syncs it. */
    suspend fun connect(
        vaultPath: String,
        remoteUrl: String,
        token: String?
    ): Outcome<PullResult, SyncError>

    /** Commits every change of the vault; false when there was nothing to commit. */
    suspend fun commitAll(
        vaultPath: String,
        message: String
    ): Outcome<Boolean, SyncError>

    suspend fun pull(
        vaultPath: String,
        token: String?
    ): Outcome<PullResult, SyncError>

    suspend fun push(
        vaultPath: String,
        token: String?
    ): Outcome<Unit, SyncError>

    /** The files changed here and not on the remote: uncommitted, or in commits not pushed. */
    suspend fun pendingChanges(vaultPath: String): Int

    /** The notes whose conflict copy waits beside them ([ConflictCopies]). */
    suspend fun conflicts(vaultPath: String): List<RelativePath>

    /** Settles [note]'s conflict as [choice] says, and commits it. */
    suspend fun resolve(
        vaultPath: String,
        note: RelativePath,
        choice: ConflictChoice
    ): Outcome<Unit, SyncError>
}

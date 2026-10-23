package app.notekea.sync.domain

/**
 * Git synchronization of the vault folder: commit, pull with rebase, push.
 * Platform implementations live outside `commonMain` (JGit on desktop, libgit2 planned for iOS);
 * no Git library type may leak through this contract.
 */
interface GitSync {
    suspend fun clone(
        remoteUrl: String,
        vaultPath: String
    )

    suspend fun sync()
}

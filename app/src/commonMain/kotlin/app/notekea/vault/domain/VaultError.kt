package app.notekea.vault.domain

/** Expected failures when opening or creating a vault; each one has a plain-language message and a way forward. */
sealed interface VaultError {
    data class CreationFailed(val path: String) : VaultError

    data class FolderAlreadyExists(val path: String) : VaultError

    data class FolderMissing(val path: String) : VaultError

    data object InvalidName : VaultError

    data class PermissionDenied(val path: String) : VaultError

    /** What was written is no address of a Git repository Notekea can reach (HTTPS, or a folder). */
    data object RepositoryBadAddress : VaultError

    /** The repository refused the token, or needs one. */
    data object RepositoryRefused : VaultError

    /** The repository could not be reached. */
    data object RepositoryOffline : VaultError

    /** Anything else Git reported while the repository was brought, as it said it. */
    data class RepositoryFailed(val message: String) : VaultError
}

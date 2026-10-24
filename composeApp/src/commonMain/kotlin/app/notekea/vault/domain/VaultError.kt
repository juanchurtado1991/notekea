package app.notekea.vault.domain

/** Expected failures when opening or creating a vault; each one has a plain-language message and a way forward. */
sealed interface VaultError {
    data class CreationFailed(val path: String) : VaultError

    data class FolderAlreadyExists(val path: String) : VaultError

    data class FolderMissing(val path: String) : VaultError

    data object InvalidName : VaultError

    data class PermissionDenied(val path: String) : VaultError
}

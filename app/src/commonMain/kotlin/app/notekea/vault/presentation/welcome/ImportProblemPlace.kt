package app.notekea.vault.presentation.welcome

import app.notekea.vault.domain.VaultError

/** The field of the import panel a problem is about, so it is said under that field and not in a banner. */
internal enum class ImportProblemPlace {
    ADDRESS,
    TOKEN,
    NAME,
    LOCATION;

    companion object {
        fun of(error: VaultError?): ImportProblemPlace? = when (error) {
            null -> null

            VaultError.RepositoryBadAddress,
            VaultError.RepositoryOffline,
            is VaultError.RepositoryFailed -> ADDRESS

            VaultError.RepositoryRefused -> TOKEN

            VaultError.InvalidName, is VaultError.FolderAlreadyExists -> NAME

            is VaultError.CreationFailed,
            is VaultError.FolderMissing,
            is VaultError.PermissionDenied -> LOCATION
        }
    }
}

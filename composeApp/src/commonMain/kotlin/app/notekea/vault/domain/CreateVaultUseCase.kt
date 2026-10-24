package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * Creates a new vault folder named [name] inside a parent folder and opens it. Names that cannot be a folder on
 * every supported operating system (empty, `.`/`..`, or containing characters Windows forbids) are rejected up
 * front, so a vault created on Linux still syncs to Windows.
 */
class CreateVaultUseCase(
    private val vaultFileSystem: VaultFileSystem,
    private val openVault: OpenVaultUseCase
) {
    suspend operator fun invoke(
        parentPath: String,
        name: String
    ): Outcome<OpenedVault, VaultError> {
        val folderName = name.trim()
        if (!isValidFolderName(name = folderName)) return Outcome.Failure(error = VaultError.InvalidName)
        return when (val created = vaultFileSystem.createFolder(parentPath = parentPath, name = folderName)) {
            is Outcome.Failure -> created
            is Outcome.Success -> openVault(path = created.value)
        }
    }

    private fun isValidFolderName(
        name: String
    ): Boolean {
        val isReserved = name.isEmpty() || name == CURRENT_FOLDER || name == PARENT_FOLDER
        return !isReserved && name.none { it in FORBIDDEN_CHARACTERS }
    }

    companion object {
        private const val CURRENT_FOLDER = "."
        private const val FORBIDDEN_CHARACTERS = "/\\:*?\"<>|"
        private const val PARENT_FOLDER = ".."
    }
}

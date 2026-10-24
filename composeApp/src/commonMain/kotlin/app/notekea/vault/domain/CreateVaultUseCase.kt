package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * Creates a new vault folder named [name] inside a parent folder and opens it. Names that cannot be a folder on
 * every supported operating system are rejected up front (see [ValidateFileNameUseCase]).
 */
class CreateVaultUseCase(
    private val vaultFileSystem: VaultFileSystem,
    private val validateFileName: ValidateFileNameUseCase,
    private val openVault: OpenVaultUseCase
) {
    suspend operator fun invoke(
        parentPath: String,
        name: String
    ): Outcome<OpenedVault, VaultError> {
        val folderName = name.trim()
        if (!validateFileName(name = folderName)) return Outcome.Failure(error = VaultError.InvalidName)
        return when (val created = vaultFileSystem.createFolder(parentPath = parentPath, name = folderName)) {
            is Outcome.Failure -> created
            is Outcome.Success -> openVault(path = created.value)
        }
    }
}

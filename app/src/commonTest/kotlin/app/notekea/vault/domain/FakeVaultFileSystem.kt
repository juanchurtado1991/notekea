package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * In-memory [VaultFileSystem]: folders not in [folders] are missing, those in [filled] have files (so a new vault there
 * is refused); [createFailure] makes creation fail.
 */
class FakeVaultFileSystem : VaultFileSystem {
    val folders = mutableMapOf<String, FolderAccess>()
    val filled = mutableSetOf<String>()
    var createFailure: VaultError? = null
    var suggestedParent = DEFAULT_PARENT

    override suspend fun access(path: String): FolderAccess = folders[path] ?: FolderAccess.MISSING

    override suspend fun createFolder(
        parentPath: String,
        name: String
    ): Outcome<String, VaultError> {
        createFailure?.let {
            return Outcome.Failure(error = it)
        }

        val path = parentPath + SEPARATOR + name

        if (path in filled) {
            return Outcome.Failure(error = VaultError.FolderAlreadyExists(path = path))
        }

        folders[path] = FolderAccess.AVAILABLE

        return Outcome.Success(value = path)
    }

    override suspend fun suggestedParentFolder(): String = suggestedParent

    companion object {
        const val DEFAULT_PARENT = "/home/ana/Documents"
        const val SEPARATOR = "/"
    }
}

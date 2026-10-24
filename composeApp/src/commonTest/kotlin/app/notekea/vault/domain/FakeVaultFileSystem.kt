package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/** In-memory [VaultFileSystem]: folders not in [folders] are missing; [createFailure] makes creation fail. */
class FakeVaultFileSystem : VaultFileSystem {
    val folders = mutableMapOf<String, FolderAccess>()
    val noteCounts = mutableMapOf<String, Int>()
    var createFailure: VaultError? = null
    var suggestedParent = DEFAULT_PARENT

    override suspend fun access(
        path: String
    ): FolderAccess = folders[path] ?: FolderAccess.MISSING

    override suspend fun countNotes(
        path: String
    ): Int = noteCounts[path] ?: 0

    override suspend fun createFolder(
        parentPath: String,
        name: String
    ): Outcome<String, VaultError> {
        createFailure?.let { return Outcome.Failure(error = it) }
        val path = parentPath + SEPARATOR + name
        folders[path] = FolderAccess.AVAILABLE
        return Outcome.Success(value = path)
    }

    override suspend fun suggestedParentFolder(): String = suggestedParent

    companion object {
        const val DEFAULT_PARENT = "/home/ana/Documents"
        const val SEPARATOR = "/"
    }
}

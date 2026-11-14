package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/** The folder operations the vault feature needs from the platform. */
interface VaultFileSystem {
    suspend fun access(path: String): FolderAccess

    /** Creates `parentPath/name`, or reuses it when it exists and is empty, and returns its path. */
    suspend fun createFolder(
        parentPath: String,
        name: String
    ): Outcome<String, VaultError>

    /**
     * Takes away the folder a failed or cancelled import was making, with what it had brought so far. Only ever
     * called for a folder [createFolder] made or found empty for that import, so nothing of the person's is in it.
     */
    suspend fun discardImport(path: String)

    /** Where new vaults are suggested: the user's Documents folder, or the home folder when there is none. */
    suspend fun suggestedParentFolder(): String
}

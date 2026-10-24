package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/** The folder operations the vault feature needs from the platform. */
interface VaultFileSystem {
    suspend fun access(
        path: String
    ): FolderAccess

    suspend fun countNotes(
        path: String
    ): Int

    /** Creates `parentPath/name`, or reuses it when it exists and is empty, and returns its path. */
    suspend fun createFolder(
        parentPath: String,
        name: String
    ): Outcome<String, VaultError>

    /** Where new vaults are suggested: the user's Documents folder, or the home folder when there is none. */
    suspend fun suggestedParentFolder(): String
}

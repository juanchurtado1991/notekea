package app.notekea.vault.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * A vault brought from a Git repository (the developer's request of 2026-11-14): a new folder named `name` inside
 * `parentPath`, the repository's notes in it, opened and left syncing. The folder must be new or empty, so nothing of
 * the person's is ever written over; when the repository cannot be brought, what was made is taken away again.
 */
class ImportVaultUseCase(
    private val vaultFileSystem: VaultFileSystem,
    private val validateFileName: ValidateFileNameUseCase,
    private val importer: VaultImporter,
    private val openVault: OpenVaultUseCase
) {
    suspend operator fun invoke(
        parentPath: String,
        name: String,
        remoteUrl: String,
        token: String?
    ): Outcome<Vault, VaultError> {
        val folderName = name.trim()
        val address = remoteUrl.trim()

        // The address first: with none, the folder has no name to follow, and its problem is the address's.
        val isAddress = importer.isAddress(remoteUrl = address)
        if (!isAddress) return Outcome.Failure(error = VaultError.RepositoryBadAddress)

        val isNamed = validateFileName(name = folderName)
        if (!isNamed) return Outcome.Failure(error = VaultError.InvalidName)

        val folder = when (
            val created = vaultFileSystem.createFolder(parentPath = parentPath, name = folderName)
        ) {
            is Outcome.Failure -> return created
            is Outcome.Success -> created.value
        }

        val imported = try {
            importer.import(
                vaultPath = folder,
                remoteUrl = address,
                token = token?.trim()?.takeIf { it.isNotEmpty() }
            )
        } catch (cancelled: CancellationException) {
            // Cancelled half way: the folder made for it goes too, so the same name can be used again.
            withContext(context = NonCancellable) { vaultFileSystem.discardImport(path = folder) }

            throw cancelled
        }

        if (imported is Outcome.Failure) {
            vaultFileSystem.discardImport(path = folder)

            return imported
        }

        return openVault(path = folder)
    }

    companion object {
        private const val GIT_SUFFIX = ".git"

        /** The folder a repository's vault is suggested to go in: its last name, "notes" of ".../me/notes.git". */
        fun nameOf(remoteUrl: String): String = remoteUrl
            .trim()
            .trimEnd('/')
            .removeSuffix(suffix = GIT_SUFFIX)
            .substringAfterLast(delimiter = '/')
            .substringAfterLast(delimiter = ':')
    }
}

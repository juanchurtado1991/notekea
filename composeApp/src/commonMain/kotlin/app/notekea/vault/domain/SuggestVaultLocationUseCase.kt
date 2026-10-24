package app.notekea.vault.domain

/** The folder where a new vault is suggested, so creating one needs no decision from first-time users. */
class SuggestVaultLocationUseCase(private val vaultFileSystem: VaultFileSystem) {
    suspend operator fun invoke(): String = vaultFileSystem.suggestedParentFolder()
}

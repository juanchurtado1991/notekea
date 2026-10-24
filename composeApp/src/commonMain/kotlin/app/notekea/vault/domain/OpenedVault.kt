package app.notekea.vault.domain

/** A vault that was just opened, with the number of Markdown notes found in it. */
data class OpenedVault(
    val vault: Vault,
    val noteCount: Int
)

package app.notekea.vault.presentation.welcome

/** The "Create a vault" panel: the folder name being typed and the folder it will be created in. */
data class NewVaultForm(
    val name: String,
    val parentPath: String
)

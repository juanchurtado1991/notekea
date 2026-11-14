package app.notekea.vault.presentation.welcome

/**
 * The "Import from a repository" panel: the repository's address, the token a private one needs, and the folder it
 * will be brought into ([name] inside [parentPath]). The name follows the address until the person types one
 * ([isNamed]).
 */
data class ImportVaultForm(
    val url: String,
    val token: String,
    val name: String,
    val parentPath: String,
    val isNamed: Boolean = false
)

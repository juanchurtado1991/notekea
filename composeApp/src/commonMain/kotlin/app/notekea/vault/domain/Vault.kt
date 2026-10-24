package app.notekea.vault.domain

/** A folder of Markdown notes. [name] is the folder's last path segment, shown to users instead of the full path. */
data class Vault(
    val path: String,
    val name: String
) {
    companion object {
        private val PATH_SEPARATORS = charArrayOf('/', '\\')

        fun fromPath(
            path: String
        ): Vault {
            val trimmedPath = path.trimEnd(*PATH_SEPARATORS)
            val lastSegment = trimmedPath.substring(
                startIndex = trimmedPath.lastIndexOfAny(chars = PATH_SEPARATORS) + 1
            )
            return Vault(
                path = path,
                name = lastSegment.ifBlank { path }
            )
        }
    }
}

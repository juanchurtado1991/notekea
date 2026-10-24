package app.notekea.vault.domain

/**
 * Whether [name] can be a file or folder name on every supported operating system: not empty, not `.`/`..`, and
 * without the characters Windows forbids. Shared by every place that names something inside a vault, so a vault
 * created or edited on Linux still syncs to Windows.
 */
class ValidateFileNameUseCase {
    operator fun invoke(
        name: String
    ): Boolean {
        val isReserved = name.isEmpty() || name == CURRENT_FOLDER || name == PARENT_FOLDER
        return !isReserved && name.none { it in FORBIDDEN_CHARACTERS }
    }

    companion object {
        private const val CURRENT_FOLDER = "."
        private const val FORBIDDEN_CHARACTERS = "/\\:*?\"<>|"
        private const val PARENT_FOLDER = ".."
    }
}

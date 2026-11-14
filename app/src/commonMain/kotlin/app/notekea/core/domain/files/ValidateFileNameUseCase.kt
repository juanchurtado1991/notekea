package app.notekea.core.domain.files

/**
 * Whether `name` can be a file or folder name on every supported operating system: not empty, not `.`/`..`, at most
 * [MAX_NAME_BYTES] bytes long (a file system's limit), and without the characters Windows forbids. Shared by every
 * feature that names a file or folder (vaults, notes), so a vault created or edited on Linux still syncs to Windows.
 */
class ValidateFileNameUseCase {
    operator fun invoke(name: String): Boolean {
        val isReserved =
            name.isEmpty() ||
                name == CURRENT_FOLDER ||
                name == PARENT_FOLDER

        val isTooLong = name.encodeToByteArray().size > MAX_NAME_BYTES

        // Names Windows keeps for devices whatever their extension ("CON.md"), or cannot end with, and control
        // characters: a note named so on another system could not be synced there.
        val stem = name
            .substringBefore(delimiter = '.')
            .trimEnd()
            .uppercase()

        val isDevice =
            stem in WINDOWS_DEVICES ||
                name.endsWith(suffix = ".") ||
                name.endsWith(suffix = " ") ||
                name.any { it < ' ' }

        return !isReserved &&
            !isTooLong &&
            !isDevice &&
            name.none { it in FORBIDDEN_CHARACTERS }
    }

    companion object {
        private const val CURRENT_FOLDER = "."

        private val WINDOWS_DEVICES = setOf("CON", "PRN", "AUX", "NUL") +
            (1..9).flatMap { listOf("COM$it", "LPT$it") }
        private const val FORBIDDEN_CHARACTERS = "/\\:*?\"<>|"
        private const val MAX_NAME_BYTES = 255
        private const val PARENT_FOLDER = ".."
    }
}

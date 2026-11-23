package app.notekea.sync.data

import java.io.File

/**
 * The files left out of the repository for their size that the person has not been told of yet: a path a line in
 * `.git/notekea-left-out`. Kept on disk, so a commit that fails afterwards or the app closing never loses it.
 */
internal object LeftOutNotice {
    private const val NAME = "notekea-left-out"
    private const val NEW_LINE = "\n"

    @Synchronized
    fun add(
        gitDir: File,
        paths: List<String>
    ) = write(
        gitDir = gitDir,
        paths = waiting(gitDir = gitDir) + paths
    )

    /** The paths waiting to be told, each once, in the order they were left out. */
    @Synchronized
    fun waiting(gitDir: File): List<String> = File(gitDir, NAME)
        .takeIf { it.isFile }
        ?.readLines()
        .orEmpty()
        .filter { it.isNotBlank() }
        .distinct()

    /** The person read [paths]: they wait no longer. */
    @Synchronized
    fun told(
        gitDir: File,
        paths: List<String>
    ) = write(
        gitDir = gitDir,
        paths = waiting(gitDir = gitDir) - paths.toSet()
    )

    private fun write(
        gitDir: File,
        paths: List<String>
    ) {
        val file = File(gitDir, NAME)

        if (paths.isEmpty()) {
            file.delete()
        } else {
            file.writeText(
                paths
                    .distinct()
                    .joinToString(
                        separator = NEW_LINE,
                        postfix = NEW_LINE
                    )
            )
        }
    }
}

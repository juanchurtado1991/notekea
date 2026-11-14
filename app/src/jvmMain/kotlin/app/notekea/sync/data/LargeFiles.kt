package app.notekea.sync.data

import co.touchlab.kermit.Logger
import org.eclipse.jgit.api.Git
import java.io.File

/**
 * Files no Git host takes (GitHub refuses any over 100 MB): a new one is left out of the repository, in its own
 * exclude file, before it is committed. Once committed, every push was refused for good and nothing else of the
 * vault synced (the code review of 2026-11-13). The file stays in the vault; it is only not synced.
 */
internal object LargeFiles {
    const val MAX_BYTES = 100L * 1024 * 1024
    private val logger = Logger.withTag(tag = "Sync")
    private val SPECIAL = Regex(pattern = "([\\\\*?\\[\\]!#])")

    /** Leaves out of [git] the files not yet in it that are over [maxBytes]; their paths. */
    fun leaveOut(
        git: Git,
        maxBytes: Long = MAX_BYTES
    ): List<String> {
        val workTree = git.repository.workTree

        val large = git
            .status()
            .call()
            .untracked
            .filter {
                File(workTree, it).length() > maxBytes
            }

        if (large.isEmpty()) return emptyList()

        val exclude = File(git.repository.directory, "info/exclude")
        exclude.parentFile.mkdirs()

        exclude.appendText(
            large
                .joinToString(
                    separator = "\n",
                    prefix = "\n",
                    postfix = "\n"
                ) {
                    "/" + it.replace(
                        regex = SPECIAL,
                        replacement = "\\\\$1"
                    )
                }
        )

        logger.w { "${large.size} file(s) over 100 MB are not synced" }

        return large
    }
}

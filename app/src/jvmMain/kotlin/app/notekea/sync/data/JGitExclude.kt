package app.notekea.sync.data

import org.eclipse.jgit.api.Git
import java.io.File

/** Paths of the vault left out of its repository through `.git/info/exclude`, which is never committed. */
internal object JGitExclude {
    private val SPECIAL = Regex(pattern = "([\\\\*?\\[\\]!#])")

    /** Adds [paths], relative to the vault, as patterns that match each of them and nothing else. */
    fun add(
        git: Git,
        paths: List<String>
    ) {
        if (paths.isEmpty()) return

        val exclude = File(git.repository.directory, "info/exclude")
        exclude.parentFile.mkdirs()

        exclude.appendText(
            paths
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
    }
}

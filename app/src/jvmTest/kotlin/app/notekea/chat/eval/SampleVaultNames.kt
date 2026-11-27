package app.notekea.chat.eval

import app.notekea.chat.domain.plan.ActionContext
import java.io.File

/** What the Sample Vault names, as committed (local edits to `smoke-data` change no number), for a request. */
internal object SampleVaultNames {
    private const val ROOT = "smoke-data/vaults/Sample Vault/"

    private val files = git("ls-files", "-z", "--", ROOT)
        .split('\u0000')
        .filter { it.endsWith(suffix = ".md") }
        .map { it.removePrefix(prefix = ROOT) }
        .sorted()

    private val folders = files
        .mapNotNull {
            it.substringBeforeLast(
                delimiter = '/',
                missingDelimiterValue = ""
            ).ifEmpty { null }
        }
        .distinct()

    private val notes = files.map { it.removeSuffix(suffix = ".md") }

    private val frontmatter = files.map {
        git("show", "HEAD:$ROOT$it")
            .substringAfter(delimiter = "---\n")
            .substringBefore(delimiter = "\n---")
    }

    private val properties = frontmatter
        .flatMap { block ->
            PROPERTY
                .findAll(input = block)
                .map {
                    it.groupValues[1]
                }
                .toList()
        }
        .filter {
            it != "tags"
        }
        .distinct()

    private val tags = frontmatter
        .flatMap { block ->
            TAGS
                .find(input = block)
                ?.groupValues
                ?.get(1)
                ?.split(",")
                .orEmpty()
        }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    fun context(openNote: String?): ActionContext = ActionContext(
        today = "2026-11-10",
        openNote = openNote?.let { name ->
            notes.firstOrNull {
                it.endsWith(suffix = "/$name") || it == name
            } ?: name
        },
        folders = folders,
        notes = notes,
        properties = properties,
        tags = tags,
        themes = listOf("workspace blue", "graphite", "sage", "lagoon", "iris", "rose", "clay", "honey")
    )

    private val PROPERTY get() = Regex(
        pattern = "^([A-Za-z_]+):",
        option = RegexOption.MULTILINE
    )
    private val TAGS get() = Regex(pattern = "tags: \\[([^]]*)]")

    private fun git(vararg arguments: String): String {
        val process =
            ProcessBuilder(listOf("git") + arguments).directory(File("..").canonicalFile).start()

        val output = process
            .inputStream
            .readBytes()
            .decodeToString()

        check(process.waitFor() == 0) { "git ${arguments.first()} failed" }

        return output
    }
}

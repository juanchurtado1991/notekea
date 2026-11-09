package app.notekea.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** What a KDoc points at exists: its `[Name]` links resolve from the file, and the project files it names are there. */
class KDocLinksTest : ArchitectureTestBase() {
    private val sources = allCodeScope.files.map {
        Source(
            path = it.path,
            packageName = it
                .packagee
                ?.name
                .orEmpty(),
            text = it.text
        )
    }

    private val declaredByPackage = sources
        .groupBy { it.packageName }
        .mapValues { (_, files) ->
            files
                .flatMapTo(destination = mutableSetOf()) { file ->
                    DECLARATION.findAll(input = file.code).map {
                        it.groupValues[1]
                    }
                }
        }

    @Test
    fun `a KDoc link names something its file can see`() {
        val broken = sources.flatMap { source ->
            val visible = declaredByPackage.getValue(key = source.packageName) + KOTLIN_NAMES

            source
                .links()
                .filterNot { link ->
                    val name = link.substringBefore(delimiter = '.')

                    val isInCode =
                        Regex(pattern = "\\b$name\\b").containsMatchIn(input = source.code)

                    name in visible || isInCode
                }
                .map { "${source.path}: [$it]" }
        }

        assertEquals(
            expected = emptyList(),
            actual = broken,
            message = "GUIDELINES.md §6: a KDoc links only what the file declares, imports or shares a package with"
        )
    }

    @Test
    fun `a project file named in a KDoc exists`() {
        val root = File(ROOT).canonicalFile

        val existing = root
            .walkTopDown()
            .onEnter {
                it.name !in SKIPPED_FOLDERS
            }
            .filter { it.isFile }
            .map {
                "/" + it.relativeTo(base = root).invariantSeparatorsPath
            }
            .toList()

        val missing = sources.flatMap { source ->
            source
                .namedFiles()
                .filterNot { name ->
                    existing.any { it.endsWith(suffix = "/$name") }
                }
                .map { "${source.path}: `$it`" }
        }

        assertEquals(
            expected = emptyList(),
            actual = missing,
            message = "GUIDELINES.md §6: a KDoc names only files that are in the repository"
        )
    }

    /** A source file: its [text], and its [code], which is the text without its comments. */
    private class Source(
        val path: String,
        val packageName: String,
        val text: String
    ) {
        val code = text.replace(
            regex = COMMENT,
            replacement = ""
        )
        private val docs = KDOC
            .findAll(input = text)
            .map { it.value }
            .toList()

        /** The names in `[Name]` links, those of code written in backticks and of Markdown links left out. */
        fun links(): List<String> = docs.flatMap { doc ->
            LINK.findAll(
                input = doc.replace(
                    regex = CODE_SPAN,
                    replacement = ""
                )
            ).map {
                it.groupValues[1]
            }
        }

        /** The project files written in backticks: code, scripts, and the documents with a name in capitals. */
        fun namedFiles(): List<String> = docs.flatMap { doc ->
            PROJECT_FILE.findAll(input = doc).map {
                it.groupValues[1]
            }
        }
    }

    private companion object {
        const val ROOT = ".."
        val CODE_SPAN = Regex(pattern = "`[^`\\n]*`")
        val COMMENT = Regex(
            pattern = "/\\*.*?\\*/|//[^\\n]*",
            option = RegexOption.DOT_MATCHES_ALL
        )
        val DECLARATION =
            Regex(pattern = "\\b(?:class|interface|object|fun|val|var|typealias)\\s+(?:<[^>]+>\\s*)?(?:[\\w.<>?, ]+\\.)?(\\w+)")
        val KDOC = Regex(
            pattern = "/\\*\\*.*?\\*/",
            option = RegexOption.DOT_MATCHES_ALL
        )
        val KOTLIN_NAMES =
            setOf("String", "Int", "Long", "Boolean", "List", "Set", "Map", "Unit", "Pair", "Throwable")
        val LINK = Regex(pattern = "(?<!\\])\\[([A-Za-z_][\\w.]*)\\](?![(\\[])")
        val PROJECT_FILE =
            Regex(pattern = "`((?:[\\w.-]+/)*(?:[\\w.-]+\\.(?:kt|kts|sh)|[A-Z][A-Z0-9_]*\\.md)|docs/[\\w./-]+)`")
        val SKIPPED_FOLDERS = setOf(".git", ".gradle", ".idea", ".kotlin", "build", "node_modules")
    }
}

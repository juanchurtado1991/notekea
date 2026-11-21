package app.notekea.core.notes.data

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The watcher's walk of the vault: hidden folders below it are skipped, the vault itself never is. */
class HiddenSkippingFileTreeVisitorTest {
    private val root: Path = Files.createTempDirectory("notekea-visitor")

    // Bug hunt, 2026-11-17: on Linux a vault in a folder named with a dot was never watched.
    private val vault: Path = root.resolve(".notes")

    @AfterTest
    fun tearDown() {
        root
            .toFile()
            .deleteRecursively()
    }

    private fun walk(
        visitor: HiddenSkippingFileTreeVisitor,
        start: Path
    ): List<String> {
        val seen = mutableListOf<String>()

        visitor.recursiveVisitFiles(
            start,
            { seen += vault.relativize(it).invariantSeparatorsPathString },
            { seen += vault.relativize(it).invariantSeparatorsPathString }
        )

        return seen.sorted()
    }

    @Test
    fun `a vault named with a dot is walked, and the hidden folders below it are not`() {
        vault
            .resolve(".git/objects")
            .createDirectories()

        vault
            .resolve("Ideas")
            .createDirectories()

        vault
            .resolve("Ideas/Plan.md")
            .writeText("# Plan\n")

        val visitor = HiddenSkippingFileTreeVisitor()

        val walked = walk(
            visitor = visitor,
            start = vault
        )

        // Folders created later: one hidden, one inside a hidden one, one plain.
        val hidden = vault.resolve(".trash").createDirectories()
        val insideHidden = vault.resolve(".git/refs").createDirectories()
        val plain = vault.resolve("Projects").createDirectories()

        val later = listOf(hidden, insideHidden, plain).map {
            walk(
                visitor = visitor,
                start = it
            )
        }

        assertEquals(
            expected = listOf(
                listOf("", "Ideas", "Ideas/Plan.md"),
                emptyList(),
                emptyList(),
                listOf("Projects")
            ),
            actual = listOf(walked) + later
        )
    }
}

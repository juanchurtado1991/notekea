package app.notekea.notes.domain

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The "Done when" of `ROADMAP.md` 1.14 on every note of `smoke-data`: its frontmatter reads as properties (but the one
 * broken on purpose), and writing each property back with the value read changes not a byte.
 */
class PropertiesRoundTripTest {
    private val reader = FrontmatterReader()
    private val patch = FrontmatterPatch()

    @Test
    fun `every smoke-data note keeps its frontmatter byte for byte when its properties are written back unchanged`() {
        val changed = notes().flatMap { file ->
            val header = NoteDocument.fromText(text = file.readText()).header
            val content = reader.read(header = header)
            val properties = (content as? FrontmatterContent.Properties)?.properties.orEmpty()
            val unchanged = listOf(FrontmatterBlocks.of(header = header)?.toHeader() ?: header) +
                properties.map { patch.set(header = header, key = it.key, value = it.value) }
            val isBrokenOnPurpose = file.name == BROKEN_ON_PURPOSE
            val invalid =
                listOfNotNull(file.path.takeIf { content is FrontmatterContent.Invalid && !isBrokenOnPurpose })
            invalid + unchanged.filter { it != header }.map { file.path + ": " + it }
        }

        assertEquals(expected = emptyList(), actual = changed)
    }

    private fun notes(): List<File> = File(SMOKE_DATA).walkTopDown()
        .filter { it.isFile && it.name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION) }
        .filterNot { file -> file.path.split(File.separator).any { it.startsWith(prefix = SCRATCH) } }
        .toList()

    companion object {
        private const val BROKEN_ON_PURPOSE = "Broken properties.md"
        private const val SCRATCH = "scratch"
        private const val SMOKE_DATA = "../smoke-data"
    }
}

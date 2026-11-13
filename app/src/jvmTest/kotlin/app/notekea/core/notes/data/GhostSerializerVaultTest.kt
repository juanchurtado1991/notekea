package app.notekea.core.notes.data

import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.FrontmatterContent
import app.notekea.core.notes.domain.frontmatter.FrontmatterReader
import app.notekea.core.notes.domain.links.LinkKind
import app.notekea.core.notes.domain.links.LinkResolver
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Ghost Serializer vault is a repository as notes: every wiki link leads to a note of the
 * vault, every note's properties can be read, and every note of a source file explains it in the same four sections.
 */
class GhostSerializerVaultTest {
    private val vault = File(GHOST_VAULT)
    private val notes = vault
        .walkTopDown()
        .filter {
            it.isFile && it.extension == MARKDOWN
        }
        .associate {
            RelativePath(value = it.relativeTo(base = vault).invariantSeparatorsPath) to
                it.readText()
        }

    @Test
    fun `every wiki link leads to a note of the vault`() {
        val resolver = LinkResolver(notePaths = notes.keys)
        val finder = JetBrainsMarkdownLinkFinder()

        val unresolved = notes.flatMap { (path, text) ->
            finder
                .find(markdown = text)
                .filter {
                    it.kind == LinkKind.WIKI
                }
                .map { it.target }
                .filter {
                    resolver.closestWiki(
                        target = it,
                        source = path
                    ) == null
                }
        }.toSet()

        assertEquals(
            expected = emptySet(),
            actual = unresolved
        )
    }

    @Test
    fun `every note's properties can be read and every source note has the four sections`() {
        val reader = FrontmatterReader()

        val invalid = notes.filter { (_, text) ->
            reader.read(
                header = NoteDocument.fromText(text = text).header
            ) is FrontmatterContent.Invalid
        }.keys

        val incomplete = notes.filter { (_, text) ->
            text.contains(other = SOURCE_PROPERTY) && SECTIONS.any { !text.contains(other = it) }
        }.keys

        assertEquals(
            expected = emptySet<RelativePath>() to emptySet(),
            actual = invalid to incomplete
        )
    }

    companion object {
        private const val GHOST_VAULT = "../smoke-data/vaults/Ghost Serializer"
        private const val MARKDOWN = "md"
        private const val SOURCE_PROPERTY = "\nsource: "
        private val SECTIONS =
            listOf("## What it is about", "## How it works", "## The code", "## Related")
    }
}

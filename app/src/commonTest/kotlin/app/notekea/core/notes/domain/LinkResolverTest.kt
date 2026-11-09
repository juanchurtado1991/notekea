package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LinkResolverTest {
    @Test
    fun `a new wiki link names a note by its name when unique, and by its path when another shares it`() {
        val resolver = LinkResolver(
            notePaths = listOf("Recipes/Arepas.md", "Ideas/Soup.md", "Recipes/Soup.md").map {
                RelativePath(value = it)
            }
        )

        assertEquals(
            expected = listOf("Arepas", "Ideas/Soup", "Recipes/Soup"),
            actual = listOf("Recipes/Arepas.md", "Ideas/Soup.md", "Recipes/Soup.md").map {
                resolver.wikiTarget(path = RelativePath(value = it))
            }
        )
    }

    // Audit: Markdown links were case-sensitive and tried only two folders, so these opened nothing and broke silently.
    @Test
    fun `a markdown link ignores letter case, then falls back on a name only one note has`() {
        val resolver = resolver("Recipes/Arepas.md", "C/Note.md", "A/b.md")

        assertEquals(
            expected = listOf(
                ResolvedLink(
                    target = RelativePath(value = "Recipes/Arepas.md"),
                    isRelativeToSource = true
                ),
                ResolvedLink(
                    target = RelativePath(value = "Recipes/Arepas.md"),
                    isRelativeToSource = false
                ),
                ResolvedLink(
                    target = RelativePath(value = "C/Note.md"),
                    isRelativeToSource = false
                )
            ),
            actual = listOf(
                resolver.resolve(
                    link = markdownLink(target = "arepas.md"),
                    source = RelativePath(value = "Recipes/x.md")
                ),
                resolver.resolve(
                    link = markdownLink(target = "recipes/AREPAS.md"),
                    source = A_NOTE
                ),
                resolver.resolve(
                    link = markdownLink(target = "Note.md"),
                    source = A_NOTE
                )
            )
        )
    }

    @Test
    fun `a markdown link prefers the path written exactly, and a name two notes share is no link`() {
        val resolver = resolver("note.md", "Note.md", "C/Soup.md", "D/Soup.md", "A/b.md")

        assertEquals(
            expected = RelativePath(value = "Note.md"),
            actual = resolver.resolve(
                link = markdownLink(target = "Note.md"),
                source = A_NOTE
            )?.target
        )

        assertEquals(
            expected = null,
            actual = resolver.resolve(
                link = markdownLink(target = "Soup.md"),
                source = A_NOTE
            )
        )
    }

    private fun resolver(vararg paths: String) = LinkResolver(
        notePaths = paths.map { RelativePath(value = it) }
    )

    private fun markdownLink(target: String) = NoteLink(
        targetStart = 0,
        targetEnd = target.length,
        target = target,
        kind = LinkKind.MARKDOWN_ANGLE
    )

    private companion object {
        val A_NOTE = RelativePath(value = "A/b.md")
    }
}

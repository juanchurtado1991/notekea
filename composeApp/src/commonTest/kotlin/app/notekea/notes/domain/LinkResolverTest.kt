package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LinkResolverTest {
    @Test
    fun `a new wiki link names a note by its name when unique, and by its path when another shares it`() {
        val resolver = LinkResolver(
            notePaths = listOf("Recipes/Arepas.md", "Ideas/Soup.md", "Recipes/Soup.md").map { RelativePath(value = it) }
        )

        assertEquals(
            expected = listOf("Arepas", "Ideas/Soup", "Recipes/Soup"),
            actual = listOf("Recipes/Arepas.md", "Ideas/Soup.md", "Recipes/Soup.md").map {
                resolver.wikiTarget(path = RelativePath(value = it))
            }
        )
    }
}

package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class EmbeddedNoteTextTest {
    private val embeddedNoteText = EmbeddedNoteText()
    private val note = """
        ---
        tags: [recipe]
        ---
        # Arepas

        Corn cakes.

        ## Dough

        Corn flour.

        ### Resting

        Ten minutes.

        ## Filling

        Cheese.
    """.trimIndent()

    @Test
    fun `a whole note is shown without its frontmatter, a section down to the next heading of its level`() {
        assertEquals(
            expected = listOf(
                note.substringAfter(delimiter = "---\n").substringAfter(delimiter = "---\n"),
                "## Dough\n\nCorn flour.\n\n### Resting\n\nTen minutes.",
                "### Resting\n\nTen minutes."
            ),
            actual = listOf("Arepas", "Arepas#dough", "Arepas#Resting").map {
                embeddedNoteText.of(text = note, target = it)
            }
        )
    }

    @Test
    fun `a heading or block the note does not have shows nothing`() {
        assertEquals(
            expected = listOf(null, null, null),
            actual = listOf("Arepas#Sauce", "Arepas#^abc123", "Arepas#^").map {
                embeddedNoteText.of(text = note, target = it)
            }
        )
    }

    @Test
    fun `a block is its list item, its paragraph, or the block above a marker on its own line, without the marker`() {
        val text = "Intro line\nSecond line of it ^para\n\n- First\n- Buy flour ^item\n- Last\n\n" +
            "| A | B |\n|---|---|\n| 1 | 2 |\n^table\n"

        assertEquals(
            expected = listOf("Intro line\nSecond line of it", "- Buy flour", "| A | B |\n|---|---|\n| 1 | 2 |"),
            actual = listOf("Note#^para", "Note#^item", "Note#^table").map {
                embeddedNoteText.of(text = text, target = it)
            }
        )
    }

    @Test
    fun `a line of a code block is not a heading, neither where the section starts nor where it ends`() {
        val text = "```\n# Setup\n```\n\n# Setup\n\n```bash\n# comment\nrun\n```\n\nAfter.\n\n# Next\n"

        assertEquals(
            expected = "# Setup\n\n```bash\n# comment\nrun\n```\n\nAfter.",
            actual = embeddedNoteText.of(text = text, target = "Note#Setup")
        )
    }

    @Test
    fun `the frontmatter is what NoteDocument says it is`() {
        assertEquals(
            expected = listOf("Body", "---\nfoo\n---bar\nBody", "Body", "Body"),
            actual = listOf(
                "---\n---\nBody",
                "---\nfoo\n---bar\nBody",
                "\uFEFF---\ntags: a\n---\nBody",
                "\uFEFFBody"
            ).map { embeddedNoteText.of(text = it, target = "Note") }
        )
    }

    @Test
    fun `a section of a note with Windows line breaks reads as any other`() {
        assertEquals(
            expected = "# One\n\nText.",
            actual = embeddedNoteText.of(text = "---\r\na: b\r\n---\r\n# One\r\n\r\nText.\r\n", target = "Note#One")
        )
    }

    @Test
    fun `a path of headings is walked in order, each one under the one before`() {
        val text = "# First\n## Child\nWrong.\n# Parent\n## Other\n### Child\nRight.\n# Last\n## Grandchild\n"

        assertEquals(
            expected = listOf("### Child\nRight.", "## Child\nWrong.", null, null),
            actual = listOf("Parent#Child", "Child", "Parent#Grandchild", "Parent#Other#Nope").map {
                embeddedNoteText.of(text = text, target = "Note#$it")
            }
        )
    }

    @Test
    fun `a heading is found whatever the punctuation and spaces, on either side`() {
        val text = "## Foo: Bar\nOne.\n## A, b  & c?\nTwo.\n## Well-known\nThree.\n"

        assertEquals(
            expected = listOf(
                "## Foo: Bar\nOne.",
                "## Foo: Bar\nOne.",
                "## A, b  & c?\nTwo.",
                "## Well-known\nThree.",
                null
            ),
            actual = listOf("Foo Bar", "foo:   bar", "a b c", "Well-known", "Well known").map {
                embeddedNoteText.of(text = text, target = "Note#$it")
            }
        )
    }
}

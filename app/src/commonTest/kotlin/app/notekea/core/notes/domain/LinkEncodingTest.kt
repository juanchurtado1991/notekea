package app.notekea.core.notes.domain

import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.notes.domain.links.percentDecoded
import app.notekea.core.notes.domain.links.spacesEncoded
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The code review of 2026-11-13: a note renamed "C# (draft)" broke every Markdown link to it, its "#" read as a
 * heading and its ")" ending the link; and a note could be named as a device of Windows, where it cannot exist.
 */
class LinkEncodingTest {
    @Test
    fun `what would break a link's target is encoded, and reads back as the name it was`() {
        val name = "Notes/C# (draft) 100%.md"
        val encoded = spacesEncoded(target = name)

        assertEquals(
            expected = listOf("Notes/C%23%20%28draft%29%20100%25.md", name),
            actual = listOf(encoded, percentDecoded(target = encoded))
        )
    }

    @Test
    fun `a name Windows keeps for a device or cannot end with is no name for a note`() {
        val validate = ValidateFileNameUseCase()
        val names = listOf("CON.md", "com1", "Notes.", "Tab\there.md", "Console.md", "Arepas.md")

        assertEquals(
            expected = listOf(false, false, false, false, true, true),
            actual = names.map { validate(name = it) }
        )
    }
}

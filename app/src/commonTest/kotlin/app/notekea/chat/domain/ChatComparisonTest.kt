package app.notekea.chat.domain

import app.notekea.chat.domain.answer.PropertyPassages
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.chat.domain.prompt.OverviewPrompt
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's answers compared with Claude's own, read from the same Sample Vault (2026-11-11), each difference as the
 * rule that fixes it: the books' properties, the notes marked with a priority and the language of the answer.
 */
class ChatComparisonTest {
    @Test
    fun `books asked in Spanish give the books' properties to answer from`() {
        val dune = app
            .notekea
            .core
            .notes
            .domain
            .NoteFixtures
            .entry(
                path = "Books/Dune.md",
                metadata = app
                    .notekea
                    .core
                    .notes
                    .domain
                    .NoteMetadata
                    .NONE
                    .copy(
                        properties = listOf(
                            NoteProperty(
                                key = "finished",
                                value = PropertyValue
                                    .Checkbox(isChecked = true)
                            )
                        )
                    )
            )

        assertEquals(
            expected = listOf("Dune — finished: true"),
            actual = PropertyPassages
                .of(
                    question = "cuantos libros terminé?",
                    notes = listOf(dune)
                )
                .map { it.text }
        )
    }

    @Test
    fun `the vault's overview names the notes marked with a priority`() {
        val launch = app
            .notekea
            .core
            .notes
            .domain
            .NoteFixtures
            .entry(
                path = "Projects/Notekea launch.md",
                metadata = app
                    .notekea
                    .core
                    .notes
                    .domain
                    .NoteMetadata
                    .NONE
                    .copy(
                        properties = listOf(
                            NoteProperty(
                                key = "priority",
                                value = PropertyValue
                                    .Number(text = "1")
                            )
                        )
                    )
            )

        assertEquals(
            expected = true,
            actual = MARKED in OverviewPrompt.mapOf(notes = listOf(launch))
        )
    }

    @Test
    fun `how many books asked in Spanish is answered in Spanish`() {
        assertEquals(
            expected = "Spanish",
            actual = SpokenLanguage.of(text = "cuántos libros terminé?")
        )
    }

    private companion object {
        const val MARKED = "Marked: Notekea launch (priority: 1)"
    }
}

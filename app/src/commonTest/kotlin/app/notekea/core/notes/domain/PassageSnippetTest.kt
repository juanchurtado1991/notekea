package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A related note's quote: its passage without headings, the sentence that shares most words lit. */
class PassageSnippetTest {
    @Test
    fun `the sentence sharing most words with the open note is the highlight, headings left out`() {
        val snippet = PassageSnippet.of(
            passage = "## Soup\nRipe tomatoes roasted with garlic. Summer tomatoes from the beds make it sweeter.",
            words = setOf("tomatoes", "beds", "summer")
        )

        assertEquals(
            expected = "Summer tomatoes from the beds make it sweeter.",
            actual = snippet
                .text
                .substring(range = snippet.highlight)
                .trim()
        )

        assertEquals(
            expected = false,
            actual = "## Soup" in snippet.text
        )
    }

    @Test
    fun `with no word in common the first sentence is the highlight`() {
        val snippet = PassageSnippet.of(
            passage = "Quartz counters. Six weeks.",
            words = setOf("garden")
        )

        assertEquals(
            expected = "Quartz counters.",
            actual = snippet
                .text
                .substring(range = snippet.highlight)
                .trim()
        )
    }

    // A passage shares many words with its note: underlining them all would underline whole lines in Related.
    @Test
    fun `the keywords are the few longest shared words, wherever they repeat`() {
        val snippet = PassageSnippet.of(
            passage = "Serializer writes the bytes of the serializer into small pooled buffers fast.",
            words = setOf("serializer", "writes", "bytes", "small", "pooled", "buffers", "fast")
        )

        assertEquals(
            expected = "serializer",
            actual = snippet
                .text
                .substring(range = snippet.keywords.first())
                .lowercase()
        )

        assertEquals(
            expected = 4,
            actual = snippet.keywords.size
        )
    }
}

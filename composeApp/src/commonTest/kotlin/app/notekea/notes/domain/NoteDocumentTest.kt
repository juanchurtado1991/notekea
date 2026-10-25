package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class NoteDocumentTest {
    @Test
    fun `frontmatter goes to the header and the body is what follows it`() {
        val document = NoteDocument.fromText(text = "---\ntags: [recipe]\n---\n# Arepas\n")

        assertEquals(
            expected = NoteDocument(header = "---\ntags: [recipe]\n---\n", body = "# Arepas\n", usesCrLf = false),
            actual = document
        )
    }

    @Test
    fun `a byte order mark stays in the header, with or without frontmatter`() {
        assertEquals(
            expected = listOf("﻿" to "# Note", "﻿---\na: 1\n---\n" to "Body"),
            actual = listOf("﻿# Note", "﻿---\na: 1\n---\nBody").map {
                NoteDocument.fromText(text = it).let { document -> document.header to document.body }
            }
        )
    }

    @Test
    fun `windows line breaks are edited as plain breaks and written back`() {
        val text = "---\r\ntags: [a]\r\n---\r\nLine one\r\nLine two\r\n"

        val document = NoteDocument.fromText(text = text)

        assertEquals(
            expected = Triple("---\r\ntags: [a]\r\n---\r\n", "Line one\nLine two\n", true),
            actual = Triple(document.header, document.body, document.usesCrLf)
        )
        assertEquals(
            expected = "---\r\ntags: [a]\r\n---\r\nLine one\r\nChanged\r\n",
            actual = document.copy(body = "Line one\nChanged\n").toText()
        )
    }

    @Test
    fun `text that only looks like frontmatter is all body`() {
        val texts = listOf(
            "---\ntitle: never closed\n# Note",
            "----\na: 1\n----\nBody",
            "Intro\n---\na: 1\n---\n",
            " ---\na: 1\n---\nBody",
            ""
        )

        assertEquals(
            expected = texts,
            actual = texts.map { NoteDocument.fromText(text = it).body }
        )
    }

    @Test
    fun `frontmatter without a body or a final line break is all header`() {
        assertEquals(
            expected = NoteDocument(header = "---\na: 1\n---", body = "", usesCrLf = false),
            actual = NoteDocument.fromText(text = "---\na: 1\n---")
        )
    }

    @Test
    fun `every note is written back exactly as it was read`() {
        val texts = listOf(
            "---\ntags: [recipe]\n---\n# Arepas\n",
            "﻿---\r\na: 1\r\n---\r\nBody\r\n",
            "Mixed\r\nline\nbreaks\r\n",
            "Lone\rcarriage return",
            "---\na: 1\n---",
            "No frontmatter at all",
            ""
        )

        assertEquals(
            expected = texts,
            actual = texts.map { NoteDocument.fromText(text = it).toText() }
        )
    }
}

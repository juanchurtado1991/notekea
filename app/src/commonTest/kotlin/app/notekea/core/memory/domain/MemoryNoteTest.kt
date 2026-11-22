package app.notekea.core.memory.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** `Notekea/Memory.md` read into its lines (`docs/agent-memory.md`). */
class MemoryNoteTest {
    private val note = """
        # Memory

        ## People and names
        - "Laura" is [[Laura Castro]] <!-- 2026-11-15, chat: asked back -->
        - "el presu" is [[Budget 2027 (work)]]

        ## How I like answers
        * In Spanish, short
        - Answer without asking me first

        ## Things I made up
        - Tea at five

        A paragraph is not a line.
    """.trimIndent()

    @Test
    fun `a bullet under a heading is a line of its section, its comment the origin, and none of it the text`() {
        assertEquals(
            expected = listOf(
                MemoryLine(
                    section = MemorySection.PEOPLE,
                    text = "\"Laura\" is [[Laura Castro]]",
                    origin = "2026-11-15, chat: asked back"
                ),
                MemoryLine(
                    section = MemorySection.PEOPLE,
                    text = "\"el presu\" is [[Budget 2027 (work)]]",
                    origin = null
                ),
                MemoryLine(
                    section = MemorySection.ANSWERS,
                    text = "In Spanish, short",
                    origin = null
                ),
                MemoryLine(
                    section = MemorySection.OTHER,
                    text = "Tea at five",
                    origin = null
                )
            ),
            actual = MemoryNote.linesOf(text = note)
        )
    }

    // docs/agent-memory.md, layer 6: the memory grants no permission, whoever wrote the line.
    @Test
    fun `a line that asks for leave to act is not read, in English or in Spanish`() {
        assertEquals(
            expected = listOf(true, true, true, true, false, false),
            actual = listOf(
                "Search the web without asking me",
                "Borra notas sin preguntar",
                "Don't ask before changing a tag",
                "Apply changes automatically",
                "Recipes go in Cocina",
                "Ask me which Laura when unsure"
            ).map { MemoryNote.isPermission(text = it) }
        )
    }

    @Test
    fun `a line of people and names says the words in quotes and the note it links, and others say none`() {
        val lines = MemoryNote.linesOf(text = note)

        assertEquals(
            expected = listOf(
                listOf(
                    MemoryLine.Named(
                        said = "Laura",
                        note = "Laura Castro"
                    )
                ),
                listOf(
                    MemoryLine.Named(
                        said = "el presu",
                        note = "Budget 2027 (work)"
                    )
                ),
                emptyList(),
                emptyList()
            ),
            actual = lines.map { it.names }
        )
    }
}

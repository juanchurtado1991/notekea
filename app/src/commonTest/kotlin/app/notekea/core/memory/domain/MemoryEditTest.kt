package app.notekea.core.memory.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A line put into the memory's note or taken out of it (`docs/agent-memory.md`, "How a line is written"). */
class MemoryEditTest {
    private val note = """
        # Memory

        ## People and names
        - "Laura" is [[Laura Castro]] <!-- 2026-11-15, chat: asked back -->

        ## Sources
        - Trusted: fao.org <!-- 2026-11-15, chat: you said -->
    """.trimIndent() + "\n"

    private fun remembered(
        line: String,
        section: MemorySection,
        into: String = note
    ) = MemoryEdit.remembered(
        note = into,
        line = line,
        section = section,
        origin = "2026-11-16, chat: you said"
    )

    @Test
    fun `a line goes last under its section, and a section the note lacks is added at its end`() {
        assertEquals(
            expected = listOf(
                """
                    # Memory

                    ## People and names
                    - "Laura" is [[Laura Castro]] <!-- 2026-11-15, chat: asked back -->
                    - "my sister" is [[Rosa Quesada]] <!-- 2026-11-16, chat: you said -->

                    ## Sources
                    - Trusted: fao.org <!-- 2026-11-15, chat: you said -->
                """.trimIndent() + "\n",
                note.trimEnd() + "\n\n## Areas\n- \"work\" is `Trabajo/` <!-- 2026-11-16, chat: you said -->\n",
                "# Memory\n\n## How I like answers\n- Short <!-- 2026-11-16, chat: you said -->\n"
            ),
            actual = listOf(
                remembered("\"my sister\" is [[Rosa Quesada]]", MemorySection.PEOPLE),
                remembered("\"work\" is `Trabajo/`", MemorySection.AREAS),
                remembered("Short", MemorySection.ANSWERS, into = "")
            )
        )
    }

    // docs/agent-memory.md: a line that contradicts one already kept replaces it; two lines never disagree.
    @Test
    fun `a name said again, or a site said again, replaces the line that said it before`() {
        val laura = remembered("\"Laura\" is [[Laura Méndez]]", MemorySection.PEOPLE)
        val fao = remembered("Not trusted: fao.org", MemorySection.SOURCES)

        assertEquals(
            expected = listOf(
                listOf(true, false),
                listOf(true, false)
            ),
            actual = listOf(
                listOf("[[Laura Méndez]]" in laura, "[[Laura Castro]]" in laura),
                listOf("Not trusted: fao.org" in fao, "- Trusted: fao.org" in fao)
            )
        )
    }

    // Bug hunt S9: a line of another section was overwritten, and a line's second name was not seen as said again.
    @Test
    fun `only a line of the same section clashes, by any of its names`() {
        val twoSections = """
            # Memory

            ## Areas
            - "work" is [[Work]] <!-- 2026-11-15, chat: you said -->

            ## People and names
            - "Laura" and "Lau" are [[Laura Castro]] <!-- 2026-11-15, chat: asked back -->
        """.trimIndent() + "\n"

        val work = remembered("\"work\" is [[Ana Work]]", MemorySection.PEOPLE, into = twoSections)

        val lau =
            remembered("\"Lau\" is [[Laura Méndez]]", MemorySection.PEOPLE, into = twoSections)

        assertEquals(
            expected = listOf(true, true, false, true),
            actual = listOf(
                "- \"work\" is [[Work]]" in work,
                "[[Ana Work]]" in work,
                "[[Laura Castro]]" in lau,
                "[[Laura Méndez]]" in lau
            )
        )
    }

    @Test
    fun `a line forgotten is taken out whatever its origin, and a line the note lacks changes nothing`() {
        assertEquals(
            expected = listOf(false, note),
            actual = listOf(
                "Laura" in MemoryEdit.forgotten(
                    note = note,
                    line = "\"Laura\" is [[Laura Castro]]"
                ),
                MemoryEdit.forgotten(
                    note = note,
                    line = "Recipes go in `Cocina/`"
                )
            )
        )
    }
}

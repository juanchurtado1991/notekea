package app.notekea.core.memory.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** What of the memory a turn is given, with no model call (`docs/agent-memory.md`, "How it is read"). */
class MemoryLinesTest {
    private val laura = line(MemorySection.PEOPLE, "\"Laura\" is [[Laura Castro]]")
    private val recipes = line(MemorySection.PLACES, "Recipes go in `Cocina/`")
    private val work = line(MemorySection.AREAS, "\"work\" is the folder `Trabajo/`")
    private val short = line(MemorySection.ANSWERS, "In Spanish, short")
    private val home = line(MemorySection.ABOUT, "I live in Bogotá, Colombia")
    private val trusted = line(MemorySection.SOURCES, "Trusted: fao.org")
    private val all = listOf(laura, recipes, work, short, home, trusted)

    private fun line(
        section: MemorySection,
        text: String
    ) = MemoryLine(
        section = section,
        text = text,
        origin = null
    )

    private fun relevant(
        task: String,
        isWeb: Boolean = false
    ) = MemoryLines.relevantTo(
        lines = all,
        task = task,
        isWeb = isWeb
    )

    @Test
    fun `how the person likes answers and who they are are always read, the sources only in web work`() {
        assertEquals(
            expected = listOf(
                listOf(short, home),
                listOf(short, home, trusted)
            ),
            actual = listOf(
                relevant(task = "what time is it in Tokyo?"),
                relevant(
                    task = "look up tomato greenhouses",
                    isWeb = true
                )
            )
        )
    }

    @Test
    fun `the other lines are read when a word of the task matches them, a plural as its singular`() {
        assertEquals(
            expected = listOf(
                listOf(laura, short, home),
                listOf(recipes, short, home),
                listOf(work, short, home),
                listOf(laura, short, home)
            ),
            actual = listOf(
                relevant(task = "what is Laura's phone?"),
                relevant(task = "write a recipe for arepas"),
                relevant(task = "search my work notes for the budget"),
                relevant(task = "¿cuál es el correo de laura?")
            )
        )
    }

    // Bug hunt S10: a two-letter name never brought its line in, and a word of the linked note did.
    @Test
    fun `a short name brings its line in, and the linked note's words do not`() {
        val jp = line(MemorySection.PEOPLE, "\"JP\" is [[Juan Pablo Ortiz]]")

        assertEquals(
            expected = listOf(listOf(jp), emptyList()),
            actual = listOf("call JP tomorrow", "call Ortiz and Castro").map {
                MemoryLines.matched(
                    lines = MemoryLines.relevantTo(
                        lines = listOf(jp, laura),
                        task = it,
                        isWeb = false
                    )
                )
            }
        )
    }

    @Test
    fun `no more than the most characters are read, and the steps show only the lines the task matched`() {
        val long = (1..40).map {
            line(MemorySection.ANSWERS, "Preference number $it, written out at length")
        }

        val read = MemoryLines.relevantTo(
            lines = long + laura,
            task = "call Laura",
            isWeb = false
        )

        assertEquals(
            expected = listOf(true, emptyList<MemoryLine>()),
            actual = listOf(
                read.sumOf {
                    it.text.length + 1
                } <= MemoryLines.MOST_CHARS,
                MemoryLines.matched(lines = listOf(short, home, trusted))
            )
        )
    }
}

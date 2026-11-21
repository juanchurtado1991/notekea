package app.notekea.chat.domain.tool.app

import kotlin.test.Test
import kotlin.test.assertEquals

/** The rows the code numbers in a note and the figure each one counts (`docs/loop-goal-plan.md`, step 3.2). */
class NoteRowsTest {
    private val body = listOf(
        "# Published 2026",
        "- 2026-02-14 — The Dublin Ledger — 1,100 words",
        "- [x] 2026-03-22 — Northlight — 2.800 palabras",
        "Some text with no figure.",
        "## Money",
        "Total booked: 3929 €",
        "Pagado: 1.234,50 €",
        "tags: travel",
        "| Mes | Ventas | Devoluciones |",
        "|---|---|---|",
        "| Octubre | 186.400 | 3.150 |"
    ).joinToString(separator = "\n")

    @Test
    fun `list items, figured Key-value lines and table rows are numbered under their headings`() {
        val rows = NoteRows.of(body = body)

        assertEquals(
            expected = listOf(
                "R1 2026-02-14 — The Dublin Ledger — 1,100 words" to "Published 2026",
                "R2 2026-03-22 — Northlight — 2.800 palabras" to "Published 2026",
                "R3 Total booked: 3929 €" to "Money",
                "R4 Pagado: 1.234,50 €" to "Money",
                "R5 Mes: Octubre | Ventas: 186.400 | Devoluciones: 3.150" to "Money"
            ),
            actual = rows.map {
                NoteRows.shownOf(row = it) to it.heading
            }
        )
    }

    @Test
    fun `a row's figure leaves dates out, reads Spanish figures, and takes the column asked`() {
        val rows = NoteRows.of(body = body)

        assertEquals(
            expected = listOf(1100.0, 2800.0, 1234.5, 3150.0, null),
            actual = listOf(
                NoteRows.figureOf(
                    row = rows[0],
                    column = ""
                ).value,
                NoteRows.figureOf(
                    row = rows[1],
                    column = ""
                ).value,
                NoteRows.figureOf(
                    row = rows[3],
                    column = ""
                ).value,
                NoteRows.figureOf(
                    row = rows[4],
                    column = "devoluciones"
                ).value,
                NoteRows.figureOf(
                    row = rows[4],
                    column = ""
                ).value
            )
        )
    }
}

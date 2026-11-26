package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The days between two days, said with which day today is and when a day of an earlier year next comes round. */
class DaysToolTest {
    private val context = ToolContext(
        vaultPath = VAULT,
        notes = emptyList(),
        task = "how many days until Andrés's birthday?",
        openNote = null,
        today = CalendarDate(
            year = 2026,
            month = 11,
            day = 11
        )
    )

    private suspend fun said(
        from: String,
        to: String
    ): String = DaysTool()
        .run(
            call = ToolCall(
                name = DaysTool.NAME,
                arguments = mapOf(
                    "from" to from,
                    "to" to to
                )
            ),
            context = context
        )
        .forModel

    // Dev, 2026-11-25: "days until the ledger cutover" was answered "the notes do not hold today's date".
    @Test
    fun `the days are said with today`() = runTest {
        assertEquals(
            expected = "days: 76 days from 2026-11-11 (Wednesday) to 2027-01-26 (Tuesday). Today is 2026-11-11 " +
                "(Wednesday).",
            actual = said(
                from = "today",
                to = "2027-01-26"
            )
        )
    }

    // Dev, 2026-11-25: a birthday written with its year of birth was counted from that year.
    @Test
    fun `a day of an earlier year says when it next comes round, this year or the next`() =
        runTest {
            assertEquals(
                expected = listOf(
                    "1983-11-25 comes round again on 2026-11-25 (Wednesday), in 14 days.",
                    "1990-03-02 comes round again on 2027-03-02 (Tuesday), in 111 days."
                ),
                actual = listOf("1983-11-25", "1990-03-02").map {
                    said(
                        from = "today",
                        to = it
                    ).substringAfter(delimiter = "(Wednesday). ")
                }
            )
        }
}

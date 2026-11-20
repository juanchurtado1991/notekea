package app.notekea.chat.domain.loop

import kotlin.test.Test
import kotlin.test.assertEquals

/** The entries of one note a task's words fit alike, told apart by the code (`docs/loop-goal-plan.md`, step 2.2). */
class FrameAlikeTest {
    private val first = "- INV-2026-034 — coastal erosion — invoice unpaid"
    private val second = "- INV-2026-038 — night bus economy — invoice unpaid"
    private val paid = "- INV-2026-021 — seaweed farming — paid"

    private val entries = listOf(first, second, paid)

    // The frame alone, 2026-11-20: the 4B marked one of two unpaid invoices "yes".
    @Test
    fun `entries writing every word of the thing are open when no task word tells them apart`() {
        assertEquals(
            expected = listOf(first, second),
            actual = FrameAlike.of(
                task = "mark the overdue invoice as paid",
                thing = "the overdue invoice",
                entries = entries
            )
        )
    }

    // The frame alone, 2026-11-20: "the 2026-11-18 confocal booking" was asked which booking, and "add vitamin D to
    // grandpa's medicines" which medicine.
    @Test
    fun `a number one of them writes tells them apart, and words every entry writes name the list`() {
        val bookings = listOf(
            "- 2026-11-13 confocal booking 09:00–12:00",
            "- 2026-11-18 confocal booking 14:00–17:00"
        )

        assertEquals(
            expected = listOf(emptyList(), emptyList()),
            actual = listOf(
                FrameAlike.of(
                    task = "change the 2026-11-18 confocal booking to 15:00–18:00",
                    thing = "the 2026-11-18 confocal booking",
                    entries = bookings + paid
                ),
                FrameAlike.of(
                    task = "add the confocal booking for Friday",
                    thing = "the confocal booking",
                    entries = bookings
                )
            )
        )
    }

    @Test
    fun `a word one of them writes alone, one entry, or no word of the thing leaves them to the marks`() {
        assertEquals(
            expected = listOf(emptyList(), emptyList(), emptyList()),
            actual = listOf(
                FrameAlike.of(
                    task = "mark the night bus invoice as paid",
                    thing = "the night bus invoice",
                    entries = entries
                ),
                FrameAlike.of(
                    task = "mark the seaweed one as done",
                    thing = "the seaweed",
                    entries = entries
                ),
                FrameAlike.of(
                    task = "move the appointment to 11:00",
                    thing = "the appointment",
                    entries = entries
                )
            )
        )
    }
}

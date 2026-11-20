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

package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a frame says the turn must do (`docs/agent-frame.md`): one thing meant and several fitting is open, one
 * fitting is settled, several meant is never asked; only the task's own words become a line of the memory.
 */
class TaskFrameTest {
    private val home = RelativePath(value = "Home/Budget.md")
    private val work = RelativePath(value = "Work/Budget.md")
    private val dentist = RelativePath(value = "Health/Dentist.md")

    private fun frame(
        count: FrameCount,
        vararg targets: FrameTarget
    ) = TaskFrame(
        targets = targets.toList(),
        count = count,
        candidates = listOf(home, work, dentist),
        words = "the budget"
    )

    @Test
    fun `one meant and two fitting is open, its options the notes' paths`() {
        val open = frame(FrameCount.ONE, FrameTarget(note = home), FrameTarget(note = work))

        assertEquals(
            expected = listOf(true, listOf("Home/Budget", "Work/Budget"), null),
            actual = listOf(open.isOpen, open.options, open.meant)
        )
    }

    @Test
    fun `one entry meant of three in a note is open, and one entry fitting is settled`() {
        val entries =
            listOf("- 12/01 10:00 cleaning", "- 12/09 16:30 filling", "- 01/14 09:00 check-up")

        val open = frame(
            FrameCount.ONE,
            FrameTarget(
                note = dentist,
                entries = entries
            )
        )

        val settled = frame(
            FrameCount.ONE,
            FrameTarget(
                note = dentist,
                entries = entries.take(n = 1)
            )
        )

        assertEquals(
            expected = listOf(true, entries, false, dentist, entries.first()),
            actual = listOf(open.isOpen, open.options, settled.isOpen, settled.meant, settled.entry)
        )
    }

    @Test
    fun `several meant is never open, and the other notes shown are the ones not meant`() {
        val both = frame(FrameCount.SEVERAL, FrameTarget(note = home), FrameTarget(note = work))
        val one = frame(FrameCount.ONE, FrameTarget(note = work))

        assertEquals(
            expected = listOf(false, true, listOf(dentist), listOf(home, dentist)),
            actual = listOf(both.isOpen, both.isSeveral, both.others, one.others)
        )
    }

    @Test
    fun `an open question is remembered by the task's own words, never by words it does not write`() {
        val open = frame(FrameCount.ONE, FrameTarget(note = home), FrameTarget(note = work))

        assertEquals(
            expected = listOf("the budget", null),
            actual = listOf(
                open.rememberingOf(task = "What is left in the budget?")?.said,
                open.rememberingOf(task = "What is left in my money note?")?.said
            )
        )
    }

    @Test
    fun `the loop is told to ask when the frame is open, and which note when it is settled`() {
        val open =
            frame(FrameCount.ONE, FrameTarget(note = home), FrameTarget(note = work)).described()

        val settled = frame(FrameCount.ONE, FrameTarget(note = work)).described()

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                "ask the person with ask_user" in open,
                "The task means Work/Budget" in settled
            )
        )
    }
}

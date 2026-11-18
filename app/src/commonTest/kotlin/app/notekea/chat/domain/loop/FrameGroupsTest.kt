package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The sets of notes a task fits alike, found by the notes' own names, and those the names alone settle
 * (`docs/agent-frame.md`): no list of words about what a task means.
 */
class FrameGroupsTest {
    private fun candidate(path: String) = FrameCandidates.Candidate(
        path = RelativePath(value = path),
        gist = "",
        entries = emptyList()
    )

    private val castro = candidate(path = "People/Laura Castro.md")
    private val mendez = candidate(path = "People/Laura Méndez.md")
    private val contacts = candidate(path = "Personal/Contactos.md")
    private val home = candidate(path = "Home/Budget 2026.md")
    private val work = candidate(path = "Work/Budget 2026.md")

    private val shown = listOf(castro, mendez, contacts, home, work)

    @Test
    fun `the notes whose names share a word the task writes are a set`() {
        assertEquals(
            expected = listOf(
                listOf(listOf(castro, mendez)),
                listOf(listOf(home, work))
            ),
            actual = listOf(
                FrameGroups.of(
                    task = "what is Laura's phone?",
                    candidates = shown
                ),
                FrameGroups.of(
                    task = "what is the total of the budget?",
                    candidates = shown
                )
            )
        )
    }

    @Test
    fun `a word of one name or folder the task writes settles the set, and none leaves it to the notes`() {
        assertEquals(
            expected = listOf(castro, work, null),
            actual = listOf(
                FrameGroups.settledOf(
                    task = "what is Laura Castro's email?",
                    group = listOf(castro, mendez)
                ),
                FrameGroups.settledOf(
                    task = "the work budget's total",
                    group = listOf(home, work)
                ),
                FrameGroups.settledOf(
                    task = "the budget's total",
                    group = listOf(home, work)
                )
            )
        )
    }
}

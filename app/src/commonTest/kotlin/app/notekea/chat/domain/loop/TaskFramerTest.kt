package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostModelJson
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The frame's small questions (`docs/agent-frame.md`): the code finds the namesakes a task's words fit, the model
 * marks each yes or no and the code counts; for an item inside a note, the same for its entries.
 */
class TaskFramerTest {
    private val engine = FakeLlmEngine()

    private val framer = TaskFramer(
        llmEngine = engine,
        modelJson = GhostModelJson()
    )

    private val home = candidate(path = "Home/Budget.md")
    private val work = candidate(path = "Work/Budget.md")

    private val dentist = candidate(
        path = "Health/Dentist.md",
        entries = listOf("- 12/01 10:00 cleaning", "- 12/09 16:30 filling")
    )

    // Two budgets whose notes each write a word of the task the other does not: only the model can tell.
    private val kitchen = candidate(
        path = "Home/Budget.md",
        words = setOf("kitchen")
    )
    private val servers = candidate(
        path = "Work/Budget.md",
        words = setOf("servers")
    )
    private val worded = "the budget for the kitchen servers"
    private val wordedOne = """{"count":"one","words":"$worded","thing":"note"}"""

    private fun candidate(
        path: String,
        entries: List<String> = emptyList(),
        words: Set<String> = emptySet()
    ) = FrameCandidates.Candidate(
        path = RelativePath(value = path),
        gist = "",
        entries = entries,
        words = words
    )

    // Each question's reply, by the prompt that asks it.
    private suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        replies: Map<String, String>,
        isChange: Boolean = true
    ): TaskFrame? {
        engine.reply = { messages -> replies[messages.first().text].orEmpty() }

        return framer.frameOf(
            task = task,
            candidates = candidates,
            memory = emptyList(),
            isChange = isChange
        )
    }

    private val one = """{"count":"one","words":"the budget","thing":"note"}"""
    private val appointment = """{"count":"one","words":"the appointment","thing":"item"}"""

    // The frame alone, 2026-11-18: the 4B marked one of two Lauras "yes" with nothing in the task to tell them apart.
    @Test
    fun `namesakes nothing tells apart stay open, and a word only one writes settles it, with no model`() =
        runTest {
            val open = frameOf(
                task = "what is the total of the budget?",
                candidates = listOf(home, work),
                replies = mapOf(FramePrompts.COUNT to one)
            )

            val told = frameOf(
                task = "what is the total of the kitchen budget?",
                candidates = listOf(kitchen, work),
                replies = mapOf(
                    FramePrompts.COUNT to """{"count":"one","words":"the kitchen budget","thing":"note"}"""
                )
            )

            assertEquals(
                expected = listOf(
                    listOf(home.path, work.path),
                    FrameReason.WORDS,
                    kitchen.path,
                    FrameReason.WORDS
                ),
                actual = listOf(open?.openNotes, open?.reason, told?.meant, told?.reason)
            )
        }

    // Dev, 2026-11-25: "when does Marco go on sabbatical?" was asked between his meeting and his note.
    @Test
    fun `notes of other kinds that share a word are no open set, and the model picks among them`() =
        runTest {
            val frame = frameOf(
                task = "when does Marco go on sabbatical?",
                candidates = listOf(
                    candidate(path = "Meetings/2026-10-28 Marco Brandt.md"),
                    candidate(path = "People/Marco Brandt.md")
                ),
                replies = mapOf(
                    FramePrompts.COUNT to """{"count":"one","words":"Marco","thing":"note"}""",
                    FramePrompts.PICK to """{"pick":"N1"}"""
                ),
                isChange = false
            )

            assertEquals(
                expected = listOf(false, "Meetings/2026-10-28 Marco Brandt.md", FrameReason.PICK),
                actual = listOf(frame?.isOpen, frame?.meant?.value, frame?.reason)
            )
        }

    // The frame alone, 2026-11-18: "add a task in Tom's note: call him about the March trip" was told apart by the
    // one Tom whose note writes March, a word of what to add, not of which note.
    @Test
    fun `only the task's words for its thing tell namesakes apart`() = runTest {
        val frame = frameOf(
            task = "add a task to the budget: ask about the kitchen",
            candidates = listOf(kitchen, work),
            replies = mapOf(FramePrompts.COUNT to one)
        )

        assertEquals(
            expected = listOf(home.path, work.path),
            actual = frame?.openNotes
        )
    }

    // docs/loop-goal-plan.md, step 2.2: asked to pick one or say "not said", the model always picked one.
    @Test
    fun `namesakes with words of their own are marked and the code counts, one settled, both or none open`() =
        runTest {
            suspend fun marked(marks: String) = frameOf(
                task = worded,
                candidates = listOf(kitchen, servers),
                replies = mapOf(FramePrompts.COUNT to wordedOne, FramePrompts.MARK_NOTES to marks)
            )

            val both = marked(marks = """{"N1":"yes","N2":"yes"}""")
            val single = marked(marks = """{"N1":"no","N2":"yes"}""")
            val none = marked(marks = """{"N1":"no","N2":"no"}""")

            assertEquals(
                expected = listOf(
                    listOf(kitchen.path, servers.path),
                    servers.path,
                    FrameReason.MARKS,
                    listOf(kitchen.path, servers.path)
                ),
                actual = listOf(both?.openNotes, single?.meant, single?.reason, none?.openNotes)
            )
        }

    @Test
    fun `a reply that does not mark every note is no frame`() = runTest {
        val frame = frameOf(
            task = worded,
            candidates = listOf(kitchen, servers),
            replies = mapOf(
                FramePrompts.COUNT to wordedOne,
                FramePrompts.MARK_NOTES to """{"N1":"yes"}"""
            )
        )

        assertEquals(
            expected = null,
            actual = frame
        )
    }

    @Test
    fun `the entries of an item are marked, one settled, two open, none the whole note`() =
        runTest {
            suspend fun marked(marks: String) = frameOf(
                task = "move the appointment to 11:00",
                candidates = listOf(dentist),
                replies = mapOf(
                    FramePrompts.COUNT to appointment,
                    FramePrompts.PICK to """{"pick":"N1"}""",
                    FramePrompts.MARK_ENTRIES to marks
                )
            )

            val two = marked(marks = """{"E1":"yes","E2":"yes"}""")
            val single = marked(marks = """{"E1":"no","E2":"yes"}""")
            val none = marked(marks = """{"E1":"no","E2":"no"}""")

            assertEquals(
                expected = listOf(dentist.entries, dentist.entries[1], FrameReason.ENTRY, false),
                actual = listOf(two?.openEntries, single?.entry, single?.reason, none?.isOpen)
            )
        }

    // The 12B's blind pool, seed 10, 2026-11-28: "move my meeting with Ms. Lee" was settled on the one meeting of
    // several the model marked.
    @Test
    fun `an entry marked is open with those that write the same task words, settled by a word of its own`() =
        runTest {
            val visits = candidate(
                path = "Health/Visits.md",
                entries = listOf("- 12/01 dentist cleaning", "- 12/09 dentist filling", "- 12/12 haircut")
            )

            suspend fun marked(task: String) = frameOf(
                task = task,
                candidates = listOf(visits),
                replies = mapOf(
                    FramePrompts.COUNT to appointment,
                    FramePrompts.PICK to """{"pick":"N1"}""",
                    FramePrompts.MARK_ENTRIES to """{"E1":"yes","E2":"no","E3":"no"}"""
                )
            )

            val shared = marked(task = "move the dentist appointment to 11:00")
            val own = marked(task = "move the dentist cleaning to 11:00")

            assertEquals(
                expected = listOf(visits.entries.take(n = 2), visits.entries[0]),
                actual = listOf(shared?.openEntries, own?.entry)
            )
        }

    // The 12B's blind pool, seed 10, 2026-11-28: "update Anna's note" was settled on the one of two Annas marked,
    // though both write an email.
    @Test
    fun `an entry marked is open with the notes of its set that hold one writing the same task words`() =
        runTest {
            val home = kitchen.copy(entries = listOf("| Parking | 40 |", "| Tiles | 300 |"))
            val work = servers.copy(entries = listOf("| Parking permit | 240 |", "| Rack | 900 |"))

            suspend fun marked(
                task: String,
                marks: String
            ) = frameOf(
                task = task,
                candidates = listOf(home, work),
                replies = mapOf(
                    FramePrompts.COUNT to """{"count":"one","words":"$worded","thing":"item"}""",
                    FramePrompts.MARK_NOTES to """{"N1":"yes","N2":"no"}""",
                    FramePrompts.MARK_ENTRIES to marks
                )
            )

            val shared = marked(
                task = "delete the parking line from $worded",
                marks = """{"E1":"yes","E2":"no"}"""
            )

            val own = marked(
                task = "delete the tiles line from $worded",
                marks = """{"E1":"no","E2":"yes"}"""
            )

            assertEquals(
                expected = listOf(listOf(home.path, work.path), home.entries[1]),
                actual = listOf(shared?.openNotes, own?.entry)
            )
        }

    // The E4B's blind pool, seed 10, 2026-11-28: every entry of "Contrato maíz" writes maíz, and the one marked was
    // left open with the others.
    @Test
    fun `the words of the note's own name tell none of its entries apart`() = runTest {
        val contract = candidate(
            path = "Contratos/Contrato maíz.md",
            entries = listOf("- Producto: maíz blanco criollo", "- Solo maíz blanco, seco")
        )

        val frame = frameOf(
            task = "ponle status firmado al contrato de maíz",
            candidates = listOf(contract),
            replies = mapOf(
                FramePrompts.COUNT to appointment,
                FramePrompts.PICK to """{"pick":"N1"}""",
                FramePrompts.MARK_ENTRIES to """{"E1":"yes","E2":"no"}"""
            )
        )

        assertEquals(
            expected = contract.entries[0],
            actual = frame?.entry
        )
    }

    // The frame alone, 2026-11-18: "total of our receipted donations" was asked which donation.
    @Test
    fun `a question about an item marks no entry, as it may add them up`() = runTest {
        frameOf(
            task = "when is the appointment?",
            candidates = listOf(dentist),
            replies = mapOf(FramePrompts.COUNT to appointment, FramePrompts.PICK to """{"pick":"N1"}"""),
            isChange = false
        )

        assertEquals(
            expected = listOf(FramePrompts.COUNT, FramePrompts.PICK),
            actual = engine.generated.map { it.first().text }
        )
    }

    @Test
    fun `the count is asked without notes, and a question about a note asks no entry`() = runTest {
        frameOf(
            task = "when is the appointment?",
            candidates = listOf(dentist),
            replies = mapOf(FramePrompts.COUNT to one, FramePrompts.PICK to """{"pick":"N1"}""")
        )

        val asked = engine.generated.map { it.first().text }

        assertEquals(
            expected = listOf(false, listOf(FramePrompts.COUNT, FramePrompts.PICK)),
            actual = listOf(
                "Health/Dentist" in engine
                    .generated
                    .first()
                    .last()
                    .text,
                asked
            )
        )
    }

    @Test
    fun `every asks nothing more, a reply cut short is no frame, and nothing shown makes no call`() =
        runTest {
            val every = frameOf(
                task = "tag my recipes",
                candidates = listOf(home),
                replies = mapOf(FramePrompts.COUNT to """{"count":"every","words":"my recipes","thing":"note"}""")
            )

            val calls = engine.generated.size

            val cut = frameOf(
                task = "tag it",
                candidates = listOf(home),
                replies = mapOf(FramePrompts.COUNT to """{"count":"o""")
            )

            val before = engine.generated.size

            val nothing = framer.frameOf(
                task = "hello",
                candidates = emptyList(),
                memory = emptyList()
            )

            assertEquals(
                expected = listOf(true, 1, null, null, before),
                actual = listOf(every?.isSeveral, calls, cut, nothing, engine.generated.size)
            )
        }

    @Test
    fun `what the task says to tell its thing from another of its name settles it`() = runTest {
        val words =
            """{"count":"one","words":"the budget","which":"the kitchen one","thing":"note"}"""

        val frame = frameOf(
            task = "what is the total of the budget, the kitchen one?",
            candidates = listOf(kitchen, work),
            replies = mapOf(FramePrompts.COUNT to words)
        )

        assertEquals(
            expected = kitchen.path,
            actual = frame?.meant
        )
    }
}

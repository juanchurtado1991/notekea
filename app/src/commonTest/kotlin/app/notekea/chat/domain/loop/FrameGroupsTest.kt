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

    private val dune = candidate(path = "Books/Dune.md")
    private val habits = candidate(path = "Books/Atomic Habits.md")
    private val shown = listOf(castro, mendez, contacts, home, work, dune, habits)

    // Dev, 2026-11-25: of the sets left open across folders by names that differ, none was right to ask.
    @Test
    fun `rivals are notes of one folder or one name, and a meeting with Marco is no rival of Marco`() {
        val marco = candidate(path = "People/Marco Brandt.md")
        val meeting = candidate(path = "Meetings/2026-10-28 Marco Brandt.md")
        val laura = candidate(path = "Contacts/Laura Castro.md")

        assertEquals(
            expected = listOf(
                emptyList(),
                listOf(castro, mendez),
                listOf(castro, laura)
            ),
            actual = listOf(
                FrameGroups.rivalsOf(group = listOf(marco, meeting)),
                FrameGroups.rivalsOf(group = listOf(castro, mendez, meeting)),
                FrameGroups.rivalsOf(group = listOf(castro, laura, home))
            )
        )
    }

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

    // The frame alone, 2026-11-18: "the food in the Azores" was open between two notes named Food.
    @Test
    fun `notes sharing one word of several the names write are no set, and one name writing them all is one`() {
        val bank = candidate(path = "Hobbies/Food bank shifts.md")
        val vocabulary = candidate(path = "Japanese/Vocabulary - Food.md")
        val azores = candidate(path = "Travel/Azores trip.md")
        val azoresFood = candidate(path = "Travel/Azores food.md")

        val none = FrameGroups.of(
            task = "the food in the azores",
            candidates = listOf(bank, vocabulary, azores)
        )

        val named = FrameGroups.of(
            task = "the food in the azores",
            candidates = listOf(bank, vocabulary, azores, azoresFood)
        )

        assertEquals(
            expected = listOf(emptyList(), listOf(listOf(azoresFood))),
            actual = listOf(none, named)
        )
    }

    // The loop's run of 2026-11-17: "the books budget" was a set of every note in Books.
    @Test
    fun `a folder's word makes no set`() {
        assertEquals(
            expected = listOf(listOf(home, work)),
            actual = FrameGroups.of(
                task = "how much of the books budget is still unspent?",
                candidates = shown
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

    // The frame alone, 2026-11-18: "mark ADR-015 as accepted" was asked which ADR.
    // The frame alone, 2026-11-18: "pon Jaén 9 como reservado" was open between the flat and its visits.
    @Test
    fun `the one note whose whole name the task writes settles the set, and two do not`() {
        val flat = candidate(path = "Pisos/Jaén 9.md")
        val visits = candidate(path = "Visitas/Visitas Jaén 9.md")

        assertEquals(
            expected = listOf(flat, null),
            actual = listOf(
                FrameGroups.settledOf(
                    task = "pon Jaén 9 como reservado",
                    group = listOf(flat, visits)
                ),
                FrameGroups.settledOf(
                    task = "the budget 2026's total",
                    group = listOf(home, work)
                )
            )
        )
    }

    @Test
    fun `a number of one name the task writes settles the set, and a number of none does not`() {
        val sprints = listOf(
            candidate(path = "Projects/Sprint 46.md"),
            candidate(path = "Projects/Sprint 47.md")
        )

        assertEquals(
            expected = listOf(sprints[1], null),
            actual = listOf(
                FrameGroups.settledOf(
                    task = "rename sprint 47 to Sprint 47 beta",
                    group = sprints
                ),
                FrameGroups.settledOf(
                    task = "add a task to the sprint for 470 users",
                    group = sprints
                )
            )
        )
    }

    // The frame alone, 2026-11-18: "Tomás from the chess club" is the Tomás whose note writes chess.
    @Test
    fun `the notes that write a task word no other writes are told apart, and none tells nothing`() {
        val herrera = FrameCandidates.Candidate(
            path = RelativePath(value = "People/Tomás Herrera.md"),
            gist = "",
            entries = emptyList(),
            words = setOf("tomas", "herrera", "japan", "phone")
        )

        val vargas = herrera.copy(
            path = RelativePath(value = "People/Tomás Vargas.md"),
            words = setOf("tomas", "vargas", "chess", "club", "phone")
        )

        assertEquals(
            expected = listOf(listOf(vargas), emptyList(), emptyList()),
            actual = listOf(
                FrameGroups.toldOf(
                    task = "the phone of Tomás from the chess club",
                    group = listOf(herrera, vargas)
                ),
                FrameGroups.toldOf(
                    task = "Tomás's phone",
                    group = listOf(herrera, vargas)
                ),
                // What it quotes is what it writes: Japan tells no Tomás apart.
                FrameGroups.toldOf(
                    task = "add 'Japan photos' to Tomás's note",
                    group = listOf(herrera, vargas)
                )
            )
        )
    }

    // Dev, 2026-11-24: "my lab budget" was open between Lab/Budget and Travel/Budget, as "lab" has three letters, and
    // "the team budget" between two whose first heading says which is the team's.
    @Test
    fun `a folder of three letters and a note's own title settle the set`() {
        val lab = candidate(path = "Lab/Budget.md")
        val travel = candidate(path = "Travel/Budget.md")
        val admin = candidate(path = "Admin/Budget.md").copy(title = "# Team budget 2026")
        val hiring = candidate(path = "Hiring/Budget.md").copy(title = "# Open roles")

        assertEquals(
            expected = listOf(lab, admin),
            actual = listOf(
                FrameGroups.settledOf(
                    task = "how much of my lab budget is paid?",
                    group = listOf(lab, travel)
                ),
                FrameGroups.settledOf(
                    task = "how much of the team budget is left?",
                    group = listOf(admin, hiring)
                )
            )
        )
    }
}

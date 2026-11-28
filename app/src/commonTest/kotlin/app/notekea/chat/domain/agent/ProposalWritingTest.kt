package app.notekea.chat.domain.agent

import app.notekea.chat.data.GhostTextNotesReader
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.CardMaker
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A proposal written out for its card, as the planner did before the loop took its place (`docs/agent-loop.md`,
 * "Step 6, finished"): the writing said as a step, and cards asked of several notes made from the ones that back any.
 */
class ProposalWritingTest {
    private val wordpress = RelativePath(value = "Work/WordPress.md")
    private val hosting = RelativePath(value = "Work/Hosting.md")

    // Only the WordPress note backs a card.
    private val cards = CardMaker { _, note ->
        if (note == wordpress) {
            Outcome.Success(value = "What powers the blog?::WordPress")
        } else {
            Outcome.Failure(error = ActionProblem.NoCards)
        }
    }

    private val check = ProposalCheck(
        checkPlan = CheckPlanUseCase(),
        composeNotes = ComposeNotesUseCase(
            llmEngine = FakeLlmEngine(),
            noteTextCache = NoteTextCache(contentStore = FakeNoteContentStore()),
            notesReader = GhostTextNotesReader(),
            cardMaker = cards
        )
    )

    private fun cardsOf(vararg names: String) = AgentProposal(
        steps = names.map {
            PlannedStep(
                kind = ActionKind.MAKE_CARDS,
                fields = mapOf(ActionField.NOTE to it)
            )
        }
    )

    private suspend fun stepsOf(proposal: AgentProposal): List<AgentStep> {
        val reported = mutableListOf<AgentStep>()

        val last = check.stepOf(
            proposal = proposal,
            unreadable = emptyList(),
            task = "study cards of my WordPress notes",
            vault = ProposalVault(
                path = VAULT,
                notes = listOf(entry(path = wordpress.value), entry(path = hosting.value)),
                openNote = null
            ),
            report = { reported += it }
        )

        return reported + last
    }

    @Test
    fun `cards of several notes leave out the one that backs none, the writing said first`() =
        runTest {
            val steps = stepsOf(proposal = cardsOf("WordPress", "Hosting"))
            val proposed = steps.last() as AgentStep.Proposed

            assertEquals(
                expected = listOf<Any?>(AgentStep.Writing(), listOf(listOf(wordpress))),
                actual = listOf(steps.first(), proposed.steps.map { it.notes })
            )
        }

    @Test
    fun `a change that writes no text says no writing`() = runTest {
        val tagged = AgentProposal(
            steps = listOf(
                PlannedStep(
                    kind = ActionKind.ADD_TAG,
                    fields = mapOf(ActionField.NOTE to "WordPress", ActionField.TAG to "web")
                )
            )
        )

        assertEquals(
            expected = emptyList(),
            actual = stepsOf(proposal = tagged).filterIsInstance<AgentStep.Writing>()
        )
    }

    @Test
    fun `cards of one note that backs none stop the turn on it`() = runTest {
        assertEquals(
            expected = AgentStep.Stopped(problem = ActionProblem.NoCards),
            actual = stepsOf(proposal = cardsOf("Hosting")).last()
        )
    }
}

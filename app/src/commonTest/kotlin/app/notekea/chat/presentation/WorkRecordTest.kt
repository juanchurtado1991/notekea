package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatAction
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.chat.presentation.viewmodel.ChatMemory
import app.notekea.chat.presentation.viewmodel.WorkRecord
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat remembers what it did, not only what it answered (the developer's smoke of 2026-11-14: asked whether it
 * had made a note up, it searched the vault and cited the made-up note as its source).
 */
class WorkRecordTest {
    private val memory = ChatMemory(compactChat = CompactChatUseCase(llmEngine = FakeLlmEngine()))
    private val contract = RelativePath(value = "Contract/contract.md")

    private fun acted(
        step: ActionStep,
        status: ActionStatus = ActionStatus.DONE,
        problem: ActionProblem? = null
    ) = ChatEntry(
        id = 1,
        question = "make a table of the contract",
        scope = ChatScope.VAULT,
        step = ChatStep.DONE,
        answer = "",
        sources = emptyList(),
        readNotes = 0,
        scopeName = null,
        outcome = TurnOutcome.ACTED,
        error = null,
        action = ChatAction(
            status = status,
            steps = listOf(step),
            problem = problem
        ),
        moves = listOf(
            AgentMoveLine(
                move = "read",
                target = "Contract/contract"
            )
        )
    )

    private fun remembered(entry: ChatEntry): String = memory
        .historyBefore(
            entries = listOf(entry),
            id = 2
        )
        .single()
        .reply

    @Test
    fun `a note written from notes is remembered as taken from them`() {
        val reply = remembered(
            entry = acted(
                step = ActionStep(
                    kind = ActionKind.WRITE_FROM_NOTES,
                    notes = listOf(contract),
                    title = "Pros and cons",
                    content = listOf(
                        NoteDraft(
                            path = RelativePath(value = "Pros and cons.md"),
                            text = "# Pros and cons\n\nA lease of 12 months."
                        )
                    )
                )
            )
        )

        assertEquals(
            expected = listOf(true, true, true, true),
            actual = listOf(
                reply.startsWith(prefix = WorkRecord.MARK),
                "I looked up: read Contract/contract." in reply,
                "I read contract.md and wrote its text from what they say" in reply,
                "A lease of 12 months." in reply
            )
        )
    }

    @Test
    fun `a note written from the request alone is remembered as the model's own example`() {
        val reply = remembered(
            entry = acted(
                step = ActionStep(
                    kind = ActionKind.CREATE_NOTE,
                    title = "Pros and cons"
                )
            )
        )

        assertEquals(
            expected = true,
            actual = "without reading any note or document: it is my own example" in reply
        )
    }

    // Code review, 2026-11-14: an agent's proposal carries a summary, and with one the record was left out.
    @Test
    fun `a proposal with a summary of its own is remembered with what was read and written too`() {
        val entry = acted(
            step = ActionStep(
                kind = ActionKind.CREATE_NOTE,
                title = "Pros and cons"
            )
        ).copy(answer = "I made the table.")

        val reply = remembered(entry = entry)

        assertEquals(
            expected = listOf(true, true, true),
            actual = listOf(
                reply.startsWith(prefix = "I made the table."),
                WorkRecord.MARK in reply,
                "my own example" in reply
            )
        )
    }

    @Test
    fun `work that failed is remembered as having written nothing`() {
        val reply = remembered(
            entry = acted(
                step = ActionStep(
                    kind = ActionKind.CREATE_NOTE,
                    title = "Pros and cons"
                ),
                status = ActionStatus.FAILED,
                problem = ActionProblem.NothingToRead(names = listOf("contract"))
            )
        )

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(
                "No note was written or changed." in reply,
                "my own example" in reply
            )
        )
    }
}

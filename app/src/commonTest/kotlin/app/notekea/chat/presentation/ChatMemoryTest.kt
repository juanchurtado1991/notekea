package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.conversation.Conversation
import app.notekea.chat.domain.conversation.ConversationMemory
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.chat.presentation.viewmodel.after
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's memory compacted (the developer's goal of 2026-11-11, and what the Auto-Test on the real app found): a
 * summary that fits the model, never longer than what it replaces, and a failure that does not leave it full.
 */
class ChatMemoryTest {
    private val engine = FakeLlmEngine().apply {
        reply = { "They asked about arepas and books." }
    }

    private fun chatOf(
        count: Long,
        answer: String
    ): Conversation = Conversation(turns = entriesOf(count, answer).map { it.toTurn() })

    private fun entriesOf(
        count: Long,
        answer: String
    ): List<ChatEntry> = (1L..count).map {
        ChatEntry(
            id = it,
            question = "question $it",
            scope = ChatScope.VAULT,
            step = ChatStep.DONE,
            answer = answer,
            sources = emptyList(),
            scopeName = null,
            outcome = TurnOutcome.ANSWERED,
            error = null
        )
    }

    // The Auto-Test on the real app, 2026-11-11: a long chat from an earlier session failed to compact, stayed full,
    // and was compacted again after every reply, the ring stuck on Compacting.
    @Test
    fun `a compaction that fails forgets the older exchanges, so the memory empties`() = runTest {
        val memory = ConversationMemory(compactChat = CompactChatUseCase(llmEngine = engine))

        val chat = chatOf(
            count = 12,
            answer = "A long answer about the notes. ".repeat(n = 20)
        )

        val before = memory.usageOf(conversation = chat)
        engine.failure = LlmError.EngineFailed
        val after = memory.compact(conversation = chat)

        assertEquals(
            expected = listOf(null, true),
            actual = listOf(after.summary, memory.usageOf(conversation = after) < before / 2)
        )
    }

    // The Auto-Test on the real app, 2026-11-11: Compact chat on three short exchanges raised the ring.
    @Test
    fun `compacting a short chat never fills the memory more`() = runTest {
        engine.reply = { LONG_SUMMARY.repeat(n = 30) }

        val memory = ConversationMemory(compactChat = CompactChatUseCase(llmEngine = engine))

        val chat = chatOf(
            count = 3,
            answer = "Two cups and a half."
        )

        val before = memory.usageOf(conversation = chat)
        val after = memory.compact(conversation = chat)

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                memory.usageOf(conversation = after) <= before,
                after.summary?.endsWith(suffix = ".") ?: true
            )
        )
    }

    @Test
    fun `compacting a long chat gives the model only its latest exchanges that fit`() = runTest {
        CompactChatUseCase(llmEngine = engine)(
            exchanges = (1..100).map {
                ChatExchange(
                    question = "question $it",
                    reply = "A long answer about the notes. ".repeat(n = 20)
                )
            },
            summary = "Earlier they asked about arepas."
        )

        val said = engine
            .generated
            .last()
            .last()
            .text

        assertEquals(
            expected = listOf(true, true, false, true),
            actual = listOf(
                said.length < 7_000,
                "question 100" in said,
                "question 1\n" in said,
                said.startsWith("Summary so far: Earlier they asked about arepas.")
            )
        )
    }

    @Test
    fun `compacting writes the earlier chat short, in the person's language`() = runTest {
        val written = CompactChatUseCase(llmEngine = engine)(
            exchanges = listOf(
                ChatExchange(
                    question = "¿Cuánta agua llevan las arepas?",
                    reply = "Dos tazas y media."
                )
            ),
            summary = null
        )

        val said = engine
            .generated
            .last()
            .last()
            .text

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                "Person: ¿Cuánta agua llevan las arepas?" in said,
                written is Outcome.Success
            )
        )
    }

    private companion object {
        const val LONG_SUMMARY =
            "They asked about the arepas, their water, their dough and how long it rests. "
    }
}

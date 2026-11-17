package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The memory and the task's own words (the loop's seventh run, 2026-11-17): "the household budget" was answered
 * from the right note and held back, as the memory says "the budget" is the other Budget 2026; and "my sister's
 * phone" was read from the note the memory names, then answered with "no note says who Rosa is".
 */
class MemoryAgainstTaskTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()

    private val guide = VaultGuide(
        noteTextCache = NoteTextCache(
            contentStore = store,
            textFolder = TextFolder()
        )
    )

    private val notes = listOf("Home/Budget 2026.md", "Work/Budget 2026.md").map {
        entry(path = it)
    }

    private val budget = MemoryLine(
        section = MemorySection.PEOPLE,
        text = "\"the budget\" is [[Work/Budget 2026]]",
        origin = null
    )

    private suspend fun meantOf(task: String) = TurnSetup
        .apartOf(
            task = task,
            vaultPath = VAULT,
            notes = notes,
            vaultGuide = guide,
            remembered = listOf(budget)
        )
        .map { it.meant.value }

    @Test
    fun `a word the task writes inside a remembered name tells its notes apart, and another word does not`() =
        runTest {
            store.texts[RelativePath(value = "Home/Budget 2026.md")] = "The household budget for the year."
            store.texts[RelativePath(value = "Work/Budget 2026.md")] = "The cooperative's total for systems."

            assertEquals(
                expected = listOf(listOf("Home/Budget 2026.md"), emptyList()),
                actual = listOf(
                    meantOf(task = "what's the total of the household budget?"),
                    meantOf(task = "what's the total of the budget?")
                )
            )
        }

    @Test
    fun `the answer is written knowing what the memory says`() = runTest {
        engine.reply = { "Her phone is **8855-2310**." }

        AgentFinalAnswer(llmEngine = engine).written(
            task = "what is my sister's phone?",
            found = listOf("read \"Rosa Quesada\":\nphone: 8855-2310"),
            summary = "",
            memory = listOf("\"my sister\" is [[Rosa Quesada]]")
        )

        val asked = engine
            .generated
            .single()
            .joinToString(separator = "\n") { it.text }

        assertEquals(
            expected = true,
            actual = "- \"my sister\" is [[Rosa Quesada]]" in asked
        )
    }
}

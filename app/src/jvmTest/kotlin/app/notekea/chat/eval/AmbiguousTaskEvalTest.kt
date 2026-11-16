package app.notekea.chat.eval

import app.notekea.chat.domain.loop.AmbiguousTask
import app.notekea.core.memory.domain.MemoryNote
import app.notekea.core.notes.domain.NoteFixtures.entry
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The loop's check of a task that can mean several notes, on every task of the evaluation with no model: it holds
 * none of the tasks that do not ask back, and most of those that should.
 */
class AmbiguousTaskEvalTest {
    private fun notesOf(vault: String) = File("../smoke-data/vaults/$vault")
        .let { root ->
            root
                .walkTopDown()
                .filter {
                    it.extension == "md"
                }
                .map { entry(path = it.relativeTo(root).invariantSeparatorsPath) }
                .toList()
        }

    private val sample = notesOf(vault = SAMPLE_VAULT)
    private val wide = notesOf(vault = AgentWideCases.VAULT)
    private val wideCases = AgentWideCases.all + AgentWideHeldOutCases.all

    @Test
    fun `no task of the Sample Vault is taken for one that can mean several notes`() {
        assertEquals(
            expected = emptyList(),
            actual = (AgentEvalCases.all + AgentHeldOutCases.all)
                .map { it.task }
                .filter {
                    AmbiguousTask.notesOf(
                        task = it,
                        notes = sample
                    ).isNotEmpty()
                }
        )
    }

    @Test
    fun `of the wider tasks, only those that ask back are held, and most of them are`() {
        val held = wideCases.filter {
            AmbiguousTask.notesOf(
                task = it.task,
                notes = wide
            ).isNotEmpty()
        }

        val asking = wideCases.filter {
            it.ends == AgentWideCase.End.ASK
        }

        assertEquals(
            expected = emptyList(),
            actual = held
                .filter {
                    it.ends != AgentWideCase.End.ASK
                }
                .map { it.task }
        )

        println(
            "held ${held.size} of ${asking.size} that ask; missed: " +
                (asking - held.toSet()).map { it.task }
        )

        assertTrue(actual = held.size * 2 >= asking.size)
    }

    // Not tuned on: it says how the check does on tasks it was not written against.
    @Test
    fun `on the unseen tasks, what is held is reported`() {
        val held = AgentUnseenCases.all.filter {
            AmbiguousTask.notesOf(
                task = it.task,
                notes = wide
            ).isNotEmpty()
        }

        val (asking, clear) = held.partition {
            it.ends == AgentWideCase.End.ASK
        }

        println(
            "unseen: held ${asking.size} that ask, ${clear.size} that do not: " +
                held.map { it.task }
        )
    }

    // docs/agent-memory.md: with a memory that says which note each name means, none of those tasks is held.
    @Test
    fun `with a memory that settles them, the unseen tasks that asked are not held`() {
        val settled = MemoryNote
            .linesOf(text = AgentMemoryCases.SETTLING)
            .mapNotNull { it.named }

        assertEquals(
            expected = emptyList(),
            actual = AgentMemoryCases
                .settled
                .map { it.task }
                .filter {
                    AmbiguousTask.notesOf(
                        task = it,
                        notes = wide,
                        settled = settled
                    ).isNotEmpty()
                }
        )
    }

    private companion object {
        const val SAMPLE_VAULT = "Sample Vault"
    }
}

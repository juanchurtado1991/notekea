package app.notekea.chat.eval

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class AgentWideCasesTest {
    private val vault = File("../smoke-data/vaults/${AgentWideCases.VAULT}")
        .walkTopDown()
        .filter {
            it.extension == "md"
        }
        .joinToString(separator = "\n") { "${it.path}\n${it.readText()}" }

    // GUIDELINES §8: every expected value of an evaluation is checked against the data it runs on.
    @Test
    fun `what a task's answer must hold is written in the vault, or the task says it is worked out`() {
        assertEquals(
            expected = emptyList(),
            actual = AgentWideCases.all.flatMap { case ->
                // A note the task writes is named by the task, not by the vault.
                case.holds.filterNot {
                    it in vault || it in case.task
                }
            }
        )
    }

    @Test
    fun `every task names tools of the design and how it ends, and every kind has tasks`() {
        assertEquals(
            expected = emptyList(),
            actual = AgentWideCases.all.flatMap {
                it.tools - AgentWideCases.TOOLS
            }
        )

        assertEquals(
            expected = AgentWideCase.Kind.entries.toSet(),
            actual = AgentWideCases
                .all
                .map { it.kind }
                .toSet()
        )
    }
}

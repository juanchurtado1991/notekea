package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A find that fails says why and where else to look: "nothing found" alone had the 9B ask for "notes tagged idea"
 * three times in the loop's fifth run.
 */
class FindFailureTest {
    private val runner = FakeActionRunner()

    private val context = ToolContext(
        vaultPath = VAULT,
        notes = emptyList(),
        task = "Archive my ideas: move the notes tagged idea into Archive/Ideas.",
        openNote = null
    )

    private suspend fun found(problem: ActionProblem): String {
        runner.problem = problem

        return FindTool(actionRunner = runner).run(
            call = ToolCall(
                name = FindTool.NAME,
                arguments = mapOf(FindTool.QUERY to "notes tagged idea")
            ),
            context = context
        ).forModel
    }

    @Test
    fun `a failed find says why, and that tags, words and links are looked up with other tools`() =
        runTest {
            assertEquals(
                expected = listOf(
                    "find_notes notes tagged idea: nothing found, as no property \"tagged\" in the vault.",
                    "find_notes notes tagged idea: nothing found, as it is not a choice by properties."
                ).map { "$it ${FoundRows.ELSEWHERE}" },
                actual = listOf(
                    found(problem = ActionProblem.UnknownQuery(property = "tagged")),
                    found(problem = ActionProblem.UnknownQuery(property = null))
                )
            )
        }
}

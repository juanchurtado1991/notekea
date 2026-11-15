package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.chat.domain.tool.app.DatePhrases
import app.notekea.core.domain.Outcome

/**
 * The notes or tasks a finding step of the app lists, as a tool's result: a line each, or that none was found; only
 * the rows [of]'s `keep` keeps, when it is given.
 */
internal class FoundRows(private val actionRunner: ActionRunner) {
    suspend fun of(
        step: ActionStep,
        context: ToolContext,
        keep: (FoundRow) -> Boolean = { true }
    ): ToolResult {
        val found = when (
            val result = actionRunner.find(context.vaultPath, step, context.notes)
        ) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> return ToolResult(forModel = "${step.kind.written}: nothing found.")
        }

        val rows = found.rows.filter(predicate = keep)

        // The query it ran, so the next call can try other words when a tag or a property was guessed wrong.
        if (rows.isEmpty()) {
            val query = found
                .query
                ?.let { " with the query:\n$it" }
                .orEmpty()

            return ToolResult(
                forModel = "${step.kind.written} ${step.request.orEmpty()}: nothing found$query."
            )
        }

        val lines = rows
            .take(n = ROWS)
            .joinToString(separator = "\n") {
                "- \"${it.note.value.removeSuffix(suffix = MARKDOWN)}\" ${cellsOf(row = it.cells)}" +
                    whenOf(row = it)
            }

        return ToolResult(forModel = "${step.kind.written} ${step.request.orEmpty()}:\n$lines")
    }

    // "(later, due 2027-01-15 (Friday))": the Tasks tab's group and the day the task writes.
    private fun whenOf(row: FoundRow): String {
        val parts = listOfNotNull(
            row.due,
            row.date?.let { "due ${DatePhrases.written(date = it)}" }
        )

        return if (parts.isEmpty()) "" else " (${parts.joinToString()})"
    }

    private fun cellsOf(row: List<String>): String = row
        .filter { it.isNotBlank() }
        .joinToString(separator = " · ")

    private companion object {
        const val MARKDOWN = ".md"
        const val ROWS = 30
    }
}

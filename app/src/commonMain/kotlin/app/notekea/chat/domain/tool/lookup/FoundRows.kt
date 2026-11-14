package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.Outcome

/** The notes or tasks a finding step of the app lists, as a tool's result: a line each, or that none was found. */
internal class FoundRows(private val actionRunner: ActionRunner) {
    suspend fun of(
        step: ActionStep,
        context: ToolContext
    ): ToolResult {
        val found = when (
            val result = actionRunner.find(context.vaultPath, step, context.notes)
        ) {
            is Outcome.Success -> result.value
            is Outcome.Failure -> return ToolResult(forModel = "${step.kind.written}: nothing found.")
        }

        val rows = found.rows

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
                "- ${it.note.nameWithoutExtension} ${it.cells.joinToString(separator = " · ")}" +
                    it
                        .due
                        ?.let { due -> " ($due)" }
                        .orEmpty()
            }

        return ToolResult(forModel = "${step.kind.written} ${step.request.orEmpty()}:\n$lines")
    }

    private companion object {
        const val ROWS = 30
    }
}

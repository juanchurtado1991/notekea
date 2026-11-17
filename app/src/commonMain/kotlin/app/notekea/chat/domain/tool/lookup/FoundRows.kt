package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
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
            is Outcome.Failure -> return ToolResult(forModel = failedOf(step, result.error))
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

        // Said, so 30 rows are not taken for all of them.
        val more = (rows.size - ROWS)
            .takeIf {
                it > 0
            }
            ?.let { "\n($it more, ${rows.size} in all: narrow the request to see them.)" }
            .orEmpty()

        return ToolResult(forModel = "${step.kind.written} ${step.request.orEmpty()}:\n$lines$more")
    }

    // Why it found nothing, and where else to look: "nothing found" alone had the 9B ask for "notes tagged idea"
    // three times (the loop's fifth run). A query only chooses by properties.
    private fun failedOf(
        step: ActionStep,
        problem: ActionProblem
    ): String {
        val asked = "${step.kind.written} ${step.request.orEmpty()}".trim()

        val why = when (problem) {
            is ActionProblem.UnknownQuery ->
                problem
                    .property
                    ?.let { "no property \"$it\" in the vault" }
                    ?: "it is not a choice by properties"

            else -> "it could not be run"
        }

        val elsewhere = if (step.kind == ActionKind.FIND_NOTES) " $ELSEWHERE" else ""

        return "$asked: nothing found, as $why.$elsewhere"
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

    companion object {
        private const val MARKDOWN = ".md"
        private const val ROWS = 30
        const val ELSEWHERE =
            "A tag is listed with list and its tag; words in the text with search; links with links; open tasks " +
                "with list, which shows each note's."
    }
}

package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.loop.FrameCount
import app.notekea.chat.domain.loop.TaskFrame

/**
 * A task's [TracedCall]s written for a report: the route, each call as the model wrote it after what it was given
 * back, and the kind of failure a task that went wrong is, so failures are counted by what to help the model with.
 */
internal object EvalTraceReport {
    /** The route the router wrote, or null when the task was not routed (today's agent). */
    fun routeOf(calls: List<TracedCall>): String? = calls
        .firstOrNull {
            it.role == TracedCall.Role.ROUTE
        }
        ?.written
        ?.let {
            WORK
                .find(input = it)
                ?.groupValues
                ?.get(1) ?: it.trim()
        }

    /** The frame in one line: how many things, settled or open, and by what (`docs/loop-goal-plan.md`, step 1.1). */
    fun frameOf(frame: TaskFrame): String {
        val by = frame
            .reason
            .name
            .lowercase()

        return when {
            frame.isOpen -> "one, open between: ${frame.options.joinToString(separator = "; ")} (by $by)"

            frame.meant != null ->
                "one, settled by $by: ${frame.meant.bare()}" +
                    frame
                        .entry
                        ?.let { ", entry \"$it\"" }
                        .orEmpty()

            frame.count == FrameCount.ONE -> "one, no note meant"

            else -> "${frame.count.written}: ${frame.notes.joinToString(separator = "; ") { it.bare() }}"
        }
    }

    /** Every call of the task, each after the results it was given that the call before was not. */
    fun traced(calls: List<TracedCall>): String = buildString {
        append("\n<details><summary>Every call</summary>\n\n")

        val shown = mutableSetOf<String>()

        calls.forEachIndexed { number, call ->
            call
                .results
                .filter { shown.add(it) }
                .forEach {
                    append("  - gave: ${flat(text = it, chars = RESULT_CHARS)}\n")
                }

            val cut = if (call.isCut) " (cut off: it ends before its JSON closes)" else ""

            append("- ${number + 1}. ${call.role.name.lowercase()}$cut: ${flat(call.written, CALL_CHARS)}\n")
        }

        append("</details>\n")
    }

    /** What kind of failure a task that went wrong is, first match wins: what the model needed help with. */
    fun categoryOf(
        problem: String,
        end: AgentStep,
        calls: List<TracedCall>
    ): String {
        val route = routeOf(calls = calls)

        val steps = calls.filter {
            it.role == TracedCall.Role.STEP
        }

        val given = steps
            .flatMap { it.results }
            .joinToString(separator = "\n")
            .plain()

        val wanted = WANTED
            .find(input = problem)
            ?.groupValues
            ?.get(1)
            ?.split(", ")
            .orEmpty()

        return when {
            route in REPLIES -> "routed to $route, a reply of the chat's own"
            isMisrouted(problem = problem, route = route) -> "routed to $route, which cannot end that way"
            end is AgentStep.Stopped -> "stopped"
            steps.any { MUST_END in it.prompt } -> "ran out of steps or repeated itself"
            problem.startsWith(prefix = "ended as") -> "ended the wrong way"
            steps.any { it.isCut } -> "wrote a call too long to end within its tokens"
            steps.count { UNREAD in it.prompt } >= 2 -> "wrote calls that could not be read"
            wanted.isEmpty() -> "did the wrong thing"
            wanted.all { it.plain() in given } -> "found it, then did not say it"
            else -> "never found it"
        }
    }

    /** The failures counted by kind, most first, each with how many tasks it holds. */
    fun summary(categories: List<String>): String = buildString {
        if (categories.isEmpty()) return@buildString

        append("\nFailures by kind:\n\n")

        categories
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .forEach { append("- ${it.value} ${it.key}\n") }
    }

    // A change asked of a turn routed to ask, or an answer asked of one routed to write or organize.
    private fun isMisrouted(
        problem: String,
        route: String?
    ): Boolean = ("not as PROPOSE" in problem && route == "ask") ||
        ("not as ANSWER" in problem && route in CHANGES)

    private fun flat(
        text: String,
        chars: Int
    ): String = text
        .replace(
            oldValue = "\n",
            newValue = " ⏎ "
        )
        .let {
            if (it.length > chars) it.take(n = chars) + " …" else it
        }

    private fun String.plain(): String = replace(
        oldValue = ",",
        newValue = ""
    ).lowercase()

    private const val RESULT_CHARS = 300
    private const val CALL_CHARS = 400
    private const val MUST_END = "end the task now"
    private const val UNREAD = "could not be read"
    private val REPLIES = setOf("talk", "help", "overview")
    private val CHANGES = setOf("write", "organize")
    private val WORK = Regex(pattern = "\"work\"\\s*:\\s*\"([a-z]+)\"")

    // The values a check said the end lacks: "does not hold [676]", "the answer misses [17, 24]".
    private val WANTED = Regex(pattern = "(?:does not hold|misses) \\[(.+?)]")
}

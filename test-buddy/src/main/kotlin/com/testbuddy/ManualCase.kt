package com.testbuddy

/**
 * A case a person checks, one step at a time in the manager: the [data] folders it needs are prepared first, then the
 * tester reads [title], follows [steps] and says whether they saw [expected], with the buttons [passLabel] and
 * [failLabel]. [launchEnvironment] is what the manager's Restart adds for this case (a crash test).
 */
public class ManualCase internal constructor(
    public val id: String,
    public val reason: ManualReason,
    public val data: List<String>,
    public val title: String,
    public val steps: String,
    public val expected: String,
    public val passLabel: String,
    public val failLabel: String,
    public val launchEnvironment: Map<String, String>
) {
    /**
     * The [steps] one by one, as the manager shows them: each line that starts with its number (`2. Click About.`)
     * begins a step, without the number; other lines belong to the step before. Steps with no numbers are one step.
     */
    public val stepList: List<String> = steps.lines().fold(initial = emptyList<String>()) { list, line ->
        val numbered = STEP_NUMBER.find(line)
        when {
            numbered != null -> list + line.removeRange(numbered.range).trim()
            list.isEmpty() -> listOf(line.trim())
            else -> list.dropLast(n = 1) + (list.last() + " " + line.trim()).trim()
        }
    }.filter { it.isNotBlank() }

    private companion object {
        val STEP_NUMBER = Regex(pattern = "^\\s*\\d+\\.\\s+")
    }
}

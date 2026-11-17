package app.notekea.chat.domain.tool

/**
 * An argument of a tool, as its grammar and its line in the prompt are written: its [name] and what it holds. A
 * [isValue] argument may be a number or true/false as well as a text: held to a string, a model wrote `"{2}"` or `"}"`
 * for a priority of 2 (the bug hunt of 2026-11-17, E2).
 */
data class ToolArgument(
    val name: String,
    val isRequired: Boolean = true,
    val isList: Boolean = false,
    val isValue: Boolean = false
)

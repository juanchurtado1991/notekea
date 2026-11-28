package app.notekea.chat.domain.tool

/**
 * An argument of a tool, as its grammar and its line in the prompt are written: its [name] and what it holds. A
 * [isValue] one may be a number or true/false too: held to a string, a model wrote `"{2}"` for a priority of 2. An
 * [isReason] one is a short sentence the model works the call out in, before the rest of it.
 */
data class ToolArgument(
    val name: String,
    val isRequired: Boolean = true,
    val isList: Boolean = false,
    val isValue: Boolean = false,
    val isReason: Boolean = false
)

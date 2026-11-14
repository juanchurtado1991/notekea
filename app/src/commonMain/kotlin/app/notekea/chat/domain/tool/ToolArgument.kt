package app.notekea.chat.domain.tool

/** An argument of a tool, as its grammar and its line in the prompt are written: its [name] and what it holds. */
data class ToolArgument(
    val name: String,
    val isRequired: Boolean = true,
    val isList: Boolean = false
)

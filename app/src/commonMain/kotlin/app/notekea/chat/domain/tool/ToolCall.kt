package app.notekea.chat.domain.tool

/** A tool as the model called it: its [name] and the [arguments] it wrote, each by its name. */
data class ToolCall(
    val name: String,
    val arguments: Map<String, String> = emptyMap()
) {
    /** The argument [name], trimmed; empty when the model left it out. */
    fun text(name: String): String = arguments[name]?.trim().orEmpty()

    /** The list argument [name], one item a line as the loop keeps it; empty when the model left it out. */
    fun list(name: String): List<String> = arguments[name]
        .orEmpty()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

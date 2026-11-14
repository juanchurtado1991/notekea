package app.notekea.chat.domain.tool

/**
 * Something the chat's model can do by naming it (`docs/agent-loop.md`): its [name], a [description] that is its
 * whole instruction, the [arguments] it takes and what it does to the turn ([effect]). The prompt's list of tools and
 * the grammar that holds a call are generated from these, so a new tool is one class and nothing else.
 */
interface ChatTool {
    val name: String
    val description: String
    val arguments: List<ToolArgument>
    val effect: ToolEffect

    /** What [call] acts on, as the step's line says it ("Read Recipes/Arepas") and as a call made twice is told. */
    fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String

    suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult
}

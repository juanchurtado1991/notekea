package app.notekea.chat.domain.tool

/** A tool that says what it was called with: its target is the note its call names, by the vault's path. */
class FakeTool(
    override val name: String,
    override val description: String,
    override val arguments: List<ToolArgument> = emptyList(),
    override val effect: ToolEffect = ToolEffect.READS
) : ChatTool {
    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String {
        val note = call.text(name = NOTE)

        return context.pathOf(name = note) ?: note
    }

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = ToolResult(forModel = "$name ${call.text(name = NOTE)}".trim())

    private companion object {
        const val NOTE = "note"
    }
}

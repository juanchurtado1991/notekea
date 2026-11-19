package app.notekea.chat.domain.tool

/** A tool whose target leaves out what makes two calls differ: "average of 2" for two averages of other items. */
interface KeyedTool {
    /** What tells [call] made twice, in place of the target. */
    fun keyOf(
        call: ToolCall,
        context: ToolContext
    ): String
}

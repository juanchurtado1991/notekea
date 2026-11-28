package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall

/** The read [call] the loop makes with [tool], and what the model is [told] in place of its own call. */
internal class ReadFirst(
    val call: ToolCall,
    val tool: ChatTool,
    val told: String
)

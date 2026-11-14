package app.notekea.chat.domain.tool

/**
 * What a tool found, as the model reads it next ([forModel]); an error is a result too, written so the next call
 * can correct it. [origin] says whether it is the person's own text.
 */
data class ToolResult(
    val forModel: String,
    val origin: ToolOrigin = ToolOrigin.VAULT
)

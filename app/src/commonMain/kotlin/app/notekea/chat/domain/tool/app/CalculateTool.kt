package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * A sum done by the app: the model writes the expression with the figures it read, and gets what it comes to, which
 * it would otherwise make up (`docs/agent-loop.md`, step 4).
 */
class CalculateTool : ChatTool {
    override val name = NAME

    override val description =
        "an arithmetic expression with the figures you read, as 4250 * 12 + 18 * 5 or 15% of 240; you get " +
            "what it comes to. Never do sums yourself."

    override val arguments = listOf(ToolArgument(name = EXPRESSION))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = EXPRESSION)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val expression = call.text(name = EXPRESSION)

        val value = Arithmetic.valueOf(expression = expression)
            ?: return ToolResult(
                forModel = "calculate \"$expression\": not arithmetic I can do. Write only numbers and " +
                    "+ - * / ( ), with no words or units."
            )

        return ToolResult(forModel = "calculate: $expression = ${Arithmetic.written(value = value)}")
    }

    companion object {
        const val NAME = "calculate"
        const val EXPRESSION = "expression"
    }
}

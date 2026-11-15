package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.Outcome

/**
 * Something the app shows at once as the turn's end (`docs/agent-loop.md`, step 6): a note or today's note opened,
 * a panel, a search, the theme. Nothing in the notes changes, so no card; a note it cannot find is told, to correct.
 */
class ShowTool(
    override val name: String,
    override val description: String,
    private val kind: ActionKind,
    private val fields: List<ActionField> = emptyList(),
    private val optional: Set<ActionField> = emptySet(),
    private val setTheme: (suspend (String) -> Boolean)? = null,
    private val checkPlan: CheckPlanUseCase = CheckPlanUseCase()
) : ChatTool {
    override val arguments = fields.map {
        ToolArgument(
            name = it.written,
            isRequired = it !in optional
        )
    }

    override val effect = ToolEffect.ENDS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = fields
        .map { call.text(name = it.written) }
        .filter { it.isNotEmpty() }
        .joinToString()

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = ToolResult(forModel = "$name: shown.")

    /** The step [call] shows, checked against the vault, or what is wrong with it for the model. */
    suspend fun shown(
        call: ToolCall,
        context: ToolContext
    ): Outcome<List<ActionStep>, String> {
        val step = PlannedStep(
            kind = kind,
            fields = fields
                .associateWith { call.text(name = it.written) }
                .filterValues { it.isNotEmpty() }
        )

        val checked = checkPlan(
            steps = listOf(step),
            notes = context.notes,
            openNote = context.openNote
        )

        if (checked is Outcome.Failure) {
            return Outcome.Failure(error = "$name: not shown, ${checked.error}. Correct it and call $name again.")
        }

        val steps = (checked as Outcome.Success).value
        val theme = step[ActionField.THEME]
        val isUnknownTheme = theme != null && setTheme?.invoke(theme) == false

        return if (isUnknownTheme) {
            Outcome.Failure(error = "$name: no theme \"$theme\". Use one of the app's themes, or ask_user.")
        } else {
            Outcome.Success(value = steps)
        }
    }
}

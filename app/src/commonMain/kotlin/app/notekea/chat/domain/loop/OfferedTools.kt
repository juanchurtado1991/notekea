package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.lookup.RecallTool
import app.notekea.chat.domain.tool.work.AskUserTool

/** The tools the model may call next: what the turn has come to leaves some out, or only one. */
internal object OfferedTools {
    /** What a call made again is told: the last one made so is not offered next. */
    const val AGAIN = "again: its result is above."

    fun of(
        state: TurnState,
        isEnding: Boolean
    ): ToolRegistry {
        // Held back for a question it must ask: only that one.
        val asking = state
            .tools
            .only(name = AskUserTool.NAME)
            .takeIf { UnsettledEnd.mustAsk(state = state) }

        if (asking != null) return asking

        // A call just made again is not offered for the next one: the model takes another way.
        val again = state
            .repeated
            .lastOrNull()
            ?.substringBefore(delimiter = ':')
            ?.takeIf {
                state
                    .found
                    .lastOrNull()
                    ?.contains(other = AGAIN) == true
            }

        // Recall is offered once something is no longer shown: before, there is nothing to ask back.
        val looking =
            if (state.hidden.isEmpty()) state.tools.without(name = RecallTool.NAME) else state.tools

        val offered = when {
            state.mustStop -> state.tools.ends()
            isEnding -> state.tools.ending()
            else -> looking
        }

        val withheld = (listOfNotNull(again) + state.withheld)
            .fold(initial = offered) { left, name -> left.without(name = name) }
            .takeIf { it.tools.isNotEmpty() }

        return withheld ?: offered
    }
}

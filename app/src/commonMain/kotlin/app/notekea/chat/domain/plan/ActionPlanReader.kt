package app.notekea.chat.domain.plan

import app.notekea.chat.domain.agent.AgentMove
import app.notekea.chat.domain.agent.AgentTaskMove
import app.notekea.chat.domain.prompt.ActionPrompt
import app.notekea.chat.domain.prompt.AgentPrompt
import app.notekea.chat.domain.prompt.TextNotesPrompt

/** Reads the JSON the model writes for actions (3.9): a plan ([ActionPrompt.SCHEMA]) or notes ([TextNotesPrompt]). */
interface ActionPlanReader {
    /** The plan's steps, or null when [json] is not a plan. */
    fun read(json: String): List<PlannedStep>?

    /** The notes as titles and texts, or null when [json] is not such a list. */
    fun readNotes(json: String): List<Pair<String, String>>?

    /** The agent's move ([AgentPrompt]), or null when [json] is not one. */
    fun readMove(json: String): AgentMove?

    /** Agent mode's next move (4.7); null when the reply is not one. */
    fun readTask(json: String): AgentTaskMove?
}

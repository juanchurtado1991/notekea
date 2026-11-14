package app.notekea.chat.domain.loop

import app.notekea.chat.domain.skill.Skill
import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException

/**
 * The first call of a turn, a cheap one: which kind of work the request is, so the loop is given only that work's
 * tools, and which skill fits it (`docs/agent-loop.md`). A reply that cannot be read is a question: [WorkKind.ASK].
 */
class TurnRouter(private val llmEngine: LlmEngine) {
    suspend fun route(
        task: String,
        skills: List<Skill>
    ): TurnRoute {
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = messagesOf(
                        task = task,
                        skills = skills
                    ),
                    options = GenerationOptions(
                        maxTokens = ROUTE_TOKENS,
                        temperature = 0f,
                        jsonSchema = schemaOf(skills = skills)
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return NONE
        }

        val fields = ToolCallJson.fields(json = json.toString()) ?: return NONE

        return TurnRoute(
            kind = WorkKind.entries.firstOrNull {
                it.written == fields[WORK]
            } ?: WorkKind.ASK,
            skill = skills.firstOrNull {
                it.name == fields[SKILL]
            }
        )
    }

    private fun messagesOf(
        task: String,
        skills: List<Skill>
    ): List<ChatMessage> {
        val kinds =
            WorkKind.entries.joinToString(separator = "\n") { "- ${it.written}: ${it.description}" }

        val ways = skills.joinToString(separator = "\n") { "- ${it.name}: ${it.description}" }

        return listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = "You say how a request to a notes app is worked on. Reply with JSON. A request that " +
                    "asks to change anything at all is not ask, even when it first looks something up " +
                    "(find my recipes and tag them: organize).\n\n" +
                    "work, one of:\n$kinds\n\nskill, a way of doing it, or $NO_SKILL when no one fits:\n$ways"
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "Request: $task"
            )
        )
    }

    private fun schemaOf(skills: List<Skill>): String {
        val kinds = WorkKind.entries.joinToString(separator = ",") { "\"${it.written}\"" }
        val names = (skills.map { it.name } + NO_SKILL).joinToString(separator = ",") { "\"$it\"" }

        return "{\"type\":\"object\",\"properties\":{\"$WORK\":{\"enum\":[$kinds]}," +
            "\"$SKILL\":{\"enum\":[$names]}},\"required\":[\"$WORK\",\"$SKILL\"]}"
    }

    private companion object {
        const val WORK = "work"
        const val SKILL = "skill"
        const val NO_SKILL = "none"
        const val ROUTE_TOKENS = 40

        val NONE = TurnRoute(
            kind = WorkKind.ASK,
            skill = null
        )
    }
}

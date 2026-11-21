package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException

/**
 * The answer checked against the task (`docs/loop-goal-plan.md`, step 5): before an answer ends the turn, a small
 * question under a grammar asks whether it says each thing the request asks. An answer that leaves something out is
 * held back once with what it leaves out; the second answer ends the turn whatever it says.
 */
internal class AnswerCheck(private val llmEngine: LlmEngine) {
    /**
     * What [call] is held back for: the loop's checks ([TurnChecks]), then the answer against the task, last and not
     * on the last step, which has no room to mend it. Null when the call goes on.
     */
    suspend fun heldOf(
        call: ToolCall,
        state: TurnState,
        isEnding: Boolean,
        tool: ChatTool?
    ): String? = TurnChecks.heldOf(
        call = call,
        state = state,
        tool = tool
    ) ?: shortOf(
        call = call,
        state = state,
        isEnding = isEnding
    )

    // What the model is told when the call answers and leaves out something the task asks, else null.
    private suspend fun shortOf(
        call: ToolCall,
        state: TurnState,
        isEnding: Boolean
    ): String? {
        val isFirst =
            call.name == AnswerTool.NAME &&
                TOLD !in state.told &&
                !isEnding

        if (!isFirst) return null

        val answer = call.text(name = AnswerTool.TEXT)

        val fields = asked(
            task = state.task,
            answer = answer
        ) ?: return null

        val missing = fields[MISSING]
            .orEmpty()
            .trim()

        val isShort = fields[SAYS] == NO && missing.isNotEmpty()
        if (!isShort || !state.told.add(TOLD)) return null

        return "(Not answered yet: the request also asks $missing, which the answer does not say. Find it with " +
            "the tools, then answer again with all of it.)"
    }

    private suspend fun asked(
        task: String,
        answer: String
    ): Map<String, String>? {
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.SYSTEM,
                            text = PROMPT
                        ),
                        ChatMessage(
                            role = ChatRole.USER,
                            text = "Request: $task\n\nAnswer: $answer"
                        )
                    ),
                    options = GenerationOptions(
                        maxTokens = TOKENS,
                        temperature = 0f,
                        jsonSchema = SCHEMA
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        return ToolCallJson.fields(json = json.toString())
    }

    private companion object {
        private const val TOLD = "answer checked"
        private const val SAYS = "says_all"
        private const val MISSING = "missing"
        private const val YES = "yes"
        private const val NO = "no"
        private const val TOKENS = 96

        private const val SCHEMA =
            "{\"type\":\"object\",\"properties\":{\"$SAYS\":{\"enum\":[\"$YES\",\"$NO\"]}," +
                "\"$MISSING\":{\"type\":\"string\"}},\"required\":[\"$SAYS\",\"$MISSING\"]}"

        private const val PROMPT =
            "You check an answer against the request it answers. A request can ask several things (a figure and a " +
                "date, who and when, each of three items). Say yes when the answer says each thing the request " +
                "asks, even briefly or in other words, or says it is not in the notes. Say no only when one thing " +
                "the request asks is not in the answer at all, and write that thing in missing in a few words. " +
                "Do not judge whether the answer is right, only whether it says each thing asked."
    }
}

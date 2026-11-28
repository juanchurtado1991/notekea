package app.notekea.chat.eval

import app.notekea.chat.domain.plan.ActionContext
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File

/** A step as the model named it, in whichever format: the action or tool, its fields and the notes it lists. */
internal data class EvalStep(
    val action: String,
    val fields: Map<String, String>,
    val notes: List<String>
) {
    /** Whether this is [wanted], a step of `smoke-data/eval/actions.json`, read as the action evaluation does. */
    fun isRight(wanted: JsonNode): Boolean {
        if (action != wanted.get("action").asText()) return false

        return wanted
            .get("fields")
            .properties()
            .all { (name, value) ->
                val planned =
                    listOfNotNull(fields[name]) + if (name == "note") notes else emptyList()

                val expected = value.asText().lowercase()

                val isFolderListed =
                    name == "folder" &&
                        notes.isNotEmpty() &&
                        notes.all { it.lowercase().startsWith(prefix = "$expected/") }

                isFolderListed || planned.any { expected.isEmpty() || expected in it.lowercase() }
            }
    }

    companion object {
        /** The step [written] as an object of fields, named by [action] or by its own "action". */
        fun of(
            written: JsonNode?,
            action: String? = null
        ): EvalStep? {
            val name = action ?: written
                ?.path(EvalTools.NAMED)
                ?.asText()
                .orEmpty()

            if (written == null || name.isEmpty()) return null

            return EvalStep(
                action = name,
                fields = written
                    .properties()
                    .filter {
                        it.value.isTextual && it.key != EvalTools.NAMED
                    }
                    .associate {
                        it.key to it.value.asText()
                    },
                notes = written
                    .path("notes")
                    .map { it.asText() }
            )
        }
    }
}

/**
 * One way to ask the model which action a request is: its [system] prompt, what it adds to the call ([request]:
 * a schema or the tools) and how its reply is [read].
 */
internal class ToolArm(
    val name: String,
    private val system: String,
    private val request: ObjectNode.(RawChatModel) -> Unit,
    private val read: (RawReply, RawChatModel) -> EvalStep?
) {
    /** The model's reply to [asked] and the step read from it. */
    fun asked(
        model: RawChatModel,
        asked: String,
        context: ActionContext
    ): Pair<RawReply, EvalStep?> {
        val user = ChatMessage(
            role = ChatRole.USER,
            text = "${context.described()}\n\nRequest: $asked"
        )

        val reply = model.reply(
            messages = listOf(
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = system
                ),
                user
            ),
            maxTokens = MAX_TOKENS
        ) { request(model) }

        return reply to read(reply, model)
    }

    companion object {
        private const val MAX_TOKENS = 400

        /** The model's own tool calls over [tools]. */
        fun native(tools: List<EvalTool>): ToolArm = native(
            name = "native calls",
            system = EvalTools.SYSTEM,
            tools = tools
        )

        private fun native(
            name: String,
            system: String,
            tools: List<EvalTool>
        ): ToolArm = ToolArm(
            name = name,
            system = system,
            request = { model ->
                set<JsonNode>("tools", model.tree(text = EvalTools.native(tools = tools)))
                put("tool_choice", "required")
                put("parallel_tool_calls", false)
            },
            read = { reply, _ ->
                EvalStep.of(
                    written = reply.arguments,
                    action = reply.tool
                )
            }
        )

        /** One call in our JSON over [tools]: the same descriptions as [native], listed in the prompt. */
        fun ours(tools: List<EvalTool>): ToolArm = ToolArm(
            name = "our JSON, a grammar per tool",
            system = EvalTools.SYSTEM + "\n" + EvalTools.listed(tools = tools),
            request = { model ->
                set<JsonNode>("json_schema", model.tree(text = EvalTools.call(tools = tools)))
            },
            read = { reply, model -> EvalStep.of(written = model.parsed(text = reply.text)) }
        )
    }
}

/** What an arm did over the evaluation's requests: a line of the report, and every reply as it came. */
internal class ArmRun(
    val arm: ToolArm,
    private val rows: List<Row>
) {
    class Row(
        val asked: String,
        val reply: RawReply,
        val wanted: JsonNode,
        val step: EvalStep?
    ) {
        val isNamed: Boolean get() = step?.action == wanted.get("action").asText()
        val isRight: Boolean get() = step?.isRight(wanted = wanted) == true
    }

    /** `| arm | action right | action and fields right | failed | prompt tokens | written tokens | ms |`. */
    fun line(label: String = arm.name): String {
        val failed = rows.count {
            it.reply.isFailed || it.step == null
        }

        return "| $label | ${rows.count { it.isNamed }} | ${rows.count { it.isRight }} of ${rows.size} | " +
            "$failed | ${median { it.promptTokens }} | ${median { it.writtenTokens }} | " +
            "${median { it.millis.toInt() }} |\n"
    }

    /** Every request with what was replied, a line each, for reading the misses. */
    fun written(file: File) {
        file.parentFile.mkdirs()

        file.writeText(
            text = rows
                .joinToString(separator = "\n") { row ->
                    val replied = row
                        .reply
                        .tool
                        ?.let { "$it ${row.reply.arguments}" } ?: row.reply.text

                    listOf(
                        row.isRight,
                        row
                            .asked
                            .lines()
                            .first()
                            .take(n = ASKED_SHOWN),
                        row
                            .wanted
                            .get("action")
                            .asText(),
                        replied.replace(
                            oldChar = '\n',
                            newChar = ' '
                        )
                    ).joinToString(separator = "\t")
                } + "\n"
        )
    }

    private fun median(of: (RawReply) -> Int): Int = rows.map {
        of(it.reply)
    }.sorted()[rows.size / 2]

    companion object {
        const val HEADER =
            "| action right | action and fields right | failed | prompt tokens | written tokens | ms |\n"

        private const val ASKED_SHOWN = 80

        /** [arm] over [requests], one after another on [model], each graded on its first step. */
        fun of(
            arm: ToolArm,
            model: RawChatModel,
            requests: List<JsonNode>
        ): ArmRun = ArmRun(
            arm = arm,
            rows = requests.map { request ->
                val asked = request.get("request").asText()

                val open = request
                    .get("open")
                    ?.takeUnless { it.isNull }
                    ?.asText()

                val (reply, step) = arm.asked(
                    model = model,
                    asked = asked,
                    context = SampleVaultNames.context(openNote = open)
                )

                Row(
                    asked = asked,
                    reply = reply,
                    wanted = request.get("steps").get(0),
                    step = step
                )
            }
        )
    }
}

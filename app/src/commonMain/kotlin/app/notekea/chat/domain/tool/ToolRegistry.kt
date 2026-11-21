package app.notekea.chat.domain.tool

/**
 * The tools one turn is given. What the model is told of them and the grammar that holds its call are generated from
 * the tools themselves: our JSON, the tool named first and then only its arguments, read right more often than the
 * model's own format or one flat schema (measured on 2026-11-14, `docs/agent-loop.md`, step 0).
 */
class ToolRegistry(val tools: List<ChatTool>) {
    private val byName = tools.associateBy { it.name }

    operator fun get(name: String): ChatTool? = byName[name]

    /** What tells a call made twice: the tool and what it acts on, however the model names it. */
    fun keyOf(
        call: ToolCall,
        context: ToolContext
    ): String = "${call.name}:${byName[call.name]?.targetOf(call, context).orEmpty()}".lowercase()

    /** The tools that end the turn and no other. */
    fun ends(): ToolRegistry = ToolRegistry(
        tools = tools.filter {
            it.effect == ToolEffect.ENDS
        }
    )

    /** The tools that change or end, none that looks: what a call may be once the turn has looked enough. */
    fun ending(): ToolRegistry = ToolRegistry(
        tools = tools.filter {
            it.effect != ToolEffect.READS
        }
    )

    /** These tools without the one named [name]: a call made again is not offered a third time. */
    fun without(name: String): ToolRegistry = ToolRegistry(
        tools = tools.filter {
            it.name != name
        }
    )

    /** The one tool named [name], when these have it: a call that may only be that one. */
    fun only(name: String): ToolRegistry? = byName[name]?.let { ToolRegistry(tools = listOf(it)) }

    /** The tools as the prompt lists them: a line each, its name, its arguments and what it does. */
    fun described(): String = tools
        .joinToString(separator = "\n") { tool ->
            val arguments = tool.arguments.joinToString { it.name }

            "- ${tool.name}${if (arguments.isEmpty()) "" else " ($arguments)"}: ${tool.description}"
        }

    /** The JSON schema of one call: any of the tools, named in [NAMED], with its own arguments and no other. */
    fun schema(): String = tools.joinToString(
        separator = ",",
        prefix = "{\"anyOf\":[",
        postfix = "]}"
    ) { schemaOf(tool = it) }

    private fun schemaOf(tool: ChatTool): String {
        val named = "\"$NAMED\":{\"const\":\"${tool.name}\"}"

        val properties = tool.arguments.map {
            val type = when {
                it.isList -> LIST
                it.isValue -> VALUE
                else -> TEXT
            }

            "\"${it.name}\":$type"
        }

        val required = listOf(NAMED) + tool
            .arguments
            .filter { it.isRequired }
            .map { it.name }

        return "{\"type\":\"object\",\"properties\":{" +
            (listOf(named) + properties).joinToString(separator = ",") +
            "},\"required\":[" +
            required.joinToString(separator = ",") { "\"$it\"" } +
            "],\"additionalProperties\":false}"
    }

    companion object {
        /** The field of a call that names its tool, written first. */
        const val NAMED = "tool"
        private const val TEXT = "{\"type\":\"string\"}"
        private const val VALUE =
            "{\"anyOf\":[{\"type\":\"string\"},{\"type\":\"number\"},{\"type\":\"boolean\"}]}"
        private const val LIST =
            "{\"type\":\"array\",\"maxItems\":12,\"items\":{\"type\":\"string\"}}"
    }
}

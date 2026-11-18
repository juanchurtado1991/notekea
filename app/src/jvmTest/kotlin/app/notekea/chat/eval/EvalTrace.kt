package app.notekea.chat.eval

import app.notekea.chat.domain.loop.FramePrompts
import app.notekea.chat.domain.loop.FrameSource
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Every model call of an evaluation's tasks, kept by task: the route, each step's call as the model wrote it and what
 * the tools and the loop gave back before it, so a report says why a task went wrong (`docs/agent-loop.md`, step 6).
 */
internal class EvalTrace(private val engine: LlmEngine) {
    private val calls = ConcurrentHashMap<String, MutableList<TracedCall>>()
    private val frames = ConcurrentHashMap<String, TaskFrame>()

    /** [engine], each call it answers kept under the task its prompt names. */
    val recorded: LlmEngine = object : LlmEngine by engine {
        override fun generate(
            messages: List<ChatMessage>,
            options: GenerationOptions
        ): Flow<String> {
            val prompt = messages.last().text
            val task = taskOf(prompt = prompt) ?: return engine.generate(messages, options)
            val written = StringBuilder()

            return engine
                .generate(
                    messages = messages,
                    options = options
                )
                .onEach { written.append(it) }
                .onCompletion {
                    callsOf(task = task) += TracedCall(
                        role = roleOf(system = messages.first().text),
                        prompt = prompt,
                        written = written.toString()
                    )
                }
        }
    }

    /** [source], each frame it builds kept under its task, for the report's frame line. */
    fun framed(source: FrameSource): FrameSource =
        FrameSource { task, vaultPath, notes, memory, openNote ->
            source.of(task, vaultPath, notes, memory, openNote)?.also { frames[task] = it }
        }

    /** The frame built for [task], or null when it had none. */
    fun frameOf(task: String): TaskFrame? = frames[task]

    fun of(task: String): List<TracedCall> = synchronized(callsOf(task = task)) {
        callsOf(task = task).toList()
    }

    /** The last prompt of the task's steps: what it had found when it ended. */
    fun lastPrompt(task: String): String? = of(task = task).lastOrNull {
        it.role != TracedCall.Role.ROUTE
    }?.prompt

    private fun callsOf(task: String): MutableList<TracedCall> = calls
        .getOrPut(task) { Collections.synchronizedList(mutableListOf()) }

    private fun taskOf(prompt: String): String? =
        (TASK.find(input = prompt) ?: REQUEST.find(input = prompt))?.groupValues?.get(1)

    private fun roleOf(system: String): TracedCall.Role = when {
        system.startsWith(prefix = ROUTER) -> TracedCall.Role.ROUTE
        system.startsWith(prefix = LOOP) -> TracedCall.Role.STEP
        system in FRAME_PROMPTS -> TracedCall.Role.FRAME
        else -> TracedCall.Role.OTHER
    }

    private companion object {
        val TASK = Regex(pattern = "Task: (.+)")

        // The frame's questions write the request after the memory and the notes, so it is found on any line.
        val REQUEST = Regex(
            pattern = "^Request: (.+)$",
            option = RegexOption.MULTILINE
        )

        val FRAME_PROMPTS = setOf(
            FramePrompts.COUNT,
            FramePrompts.MARK_NOTES,
            FramePrompts.PICK,
            FramePrompts.MARK_ENTRIES,
            FramePrompts.SEVERAL
        )
        const val ROUTER = "You say how a request"
        const val LOOP = "You work on a task"
    }
}

/** One model call of a task: the router's, a step of the loop's, or another's (an answer written from notes). */
internal class TracedCall(
    val role: Role,
    val prompt: String,
    val written: String
) {
    enum class Role { ROUTE, FRAME, STEP, OTHER }

    /** A call of JSON that stops before it closes: the model ran out of tokens while writing it. */
    val isCut: Boolean
        get() =
            role != Role.OTHER &&
                written.trimStart().startsWith(prefix = "{") &&
                !written.trimEnd().endsWith(suffix = "}")

    /** What the tools and the loop gave back so far, one part a call; empty for a call that is not a step. */
    val results: List<String>
        get() = if (role != Role.STEP) {
            emptyList()
        } else {
            prompt
                .substringAfter(
                    delimiter = RESULTS,
                    missingDelimiterValue = ""
                )
                .substringBefore(delimiter = STEPS_LEFT)
                .split("\n\n")
                .filter {
                    it.isNotBlank() && it != NOTHING
                }
        }

    private companion object {
        const val RESULTS = "Results so far:\n"
        const val STEPS_LEFT = "\n\nSteps left:"
        const val NOTHING = "Nothing yet."
    }
}

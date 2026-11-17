package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.AgentTaskMove
import app.notekea.chat.domain.agent.GroundedSteps
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.agent.ProposalVault
import app.notekea.chat.domain.agent.Remembering
import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.context.ModelProfile
import app.notekea.chat.domain.context.StepMemory
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.app.ShowTool
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.lookup.RecallTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.chat.domain.tool.work.HandTool
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemoryLines
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.web.domain.Countries
import app.notekea.core.web.domain.SourceTrust
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * The one loop (`docs/agent-loop.md`, step 5): the model is given the task and a small set of tools, calls one, sees
 * what it really gave back, and goes on until it answers, proposes or asks, [MAX_STEPS] calls in [TIME_LIMIT] at
 * most. A call that cannot be read or names no tool is a result it can correct, twice at most.
 */
class RunTurnUseCase(
    private val llmEngine: LlmEngine,
    private val toolsets: Toolsets,
    private val vaultGuide: VaultGuide,
    private val proposal: ProposalCheck,
    private val clock: Clock,
    private val stepMemory: StepMemory? = null
) {
    private val finalAnswer = AgentFinalAnswer(llmEngine = llmEngine)
    private val router = TurnRouter(llmEngine = llmEngine)

    operator fun invoke(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        context: ActionContext,
        openNote: RelativePath?
    ): Flow<AgentStep> = flow {
        val remembered = vaultGuide.memoryOf(vaultPath, notes)
        val turn = TurnSetup.contextOf(task, vaultPath, notes, context.today, remembered, openNote)

        val route = router.route(
            task = task,
            skills = vaultGuide.skillsOf(vaultPath, notes)
        )

        if (route.kind.isReply) return@flow emit(value = AgentStep.Replied(kind = route.kind))

        val tools = StepRecall.toolsOf(
            tools = toolsets.of(
                kind = route.kind,
                task = task
            ),
            memory = stepMemory
        )

        val memory = TurnSetup.memoryOf(
            task = task,
            remembered = remembered,
            kind = route.kind
        )

        // A line the task matched is said as a step, so a wrong memory is seen the first time it matters.
        MemoryLines.matched(lines = memory).forEach {
            emit(
                value = AgentStep.Moving(
                    number = 0,
                    move = MemoryLines.MOVE,
                    target = it.text
                )
            )
        }

        val system = LoopPrompt.system(
            tools = tools,
            instructions = vaultGuide.instructionsOf(vaultPath, notes),
            skill = route.skill,
            memory = memory
        )

        val state = TurnState(
            task = task,
            context = context,
            system = system,
            tools = tools,
            turn = turn,
            memory = memory,
            budget = ContextBudget(
                llmEngine = llmEngine,
                tokens = ModelProfile.of(llmEngine = llmEngine).loopTokens,
                fixed = LoopPrompt.fixedOf(system, task, context),
                replyTokens = CALL_TOKENS + WORK_TOKENS,
                memory = stepMemory,
                need = task,
                space = Random.nextLong().toString()
            )
        )

        try {
            run(state = state)
        } finally {
            withContext(context = NonCancellable) { state.budget.forget() }
        }
    }

    private suspend fun FlowCollector<AgentStep>.run(state: TurnState) {
        val deadline = clock.now() + TIME_LIMIT

        for (number in 1..MAX_STEPS) {
            if (clock.now() > deadline) break
            val isEnding = state.mustEnd || number == MAX_STEPS

            val call = callOf(
                state = state,
                left = MAX_STEPS - number + 1,
                isEnding = isEnding
            ) ?: return emit(value = unread(state = state))

            val tool = state.tools[call.name]

            val held = TurnChecks.heldOf(
                call = call,
                state = state,
                tool = tool
            )

            when {
                held != null -> state.found += held

                tool == null -> Unit

                tool is HandTool -> return emit(value = AgentStep.Replied(kind = tool.kind))

                tool is ShowTool -> when (val shown = tool.shown(call, state.turn)) {
                    is Outcome.Success -> return emit(value = AgentStep.Shown(steps = shown.value))
                    is Outcome.Failure -> state.found += shown.error
                }

                tool.effect == ToolEffect.ENDS -> return ended(
                    call = call,
                    tool = tool,
                    state = state
                )

                else -> made(
                    number = number,
                    call = call,
                    tool = tool,
                    state = state
                )
            }

            // New sources found on the web wait for the person, unless this turn already says which to read.
            val web = state.turn.web
            val isApproved = UrlsNamed.writtenIn(text = state.task).isNotEmpty()

            if (web.isWaiting && !isApproved) {
                return emit(
                    value = AgentStep.SourcesFound(
                        sources = web.found,
                        omitted = web.omitted
                    )
                )
            }
        }

        // Held on the last step for the question it must ask: one call more, which may only ask it.
        val asked = if (UnsettledEnd.mustAsk(state = state)) {
            callOf(
                state,
                left = 1,
                isEnding = true
            )
        } else {
            null
        }

        val asking = asked?.let {
            state.tools[it.name]
        }

        val isAsked = asked?.name == AskUserTool.NAME && asking != null

        if (isAsked) {
            return ended(
                call = asked,
                tool = asking,
                state = state
            )
        }

        emit(value = unread(state = state))
    }

    // What the answer is written from: what was found, as far as the context goes.
    private suspend fun fittedOf(state: TurnState): List<String> = state
        .budget
        .fit(state.found, state.dropped)
        .entries

    // A call that reads or works: shown as a step, run once, and told when it is made again.
    private suspend fun FlowCollector<AgentStep>.made(
        number: Int,
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ) {
        val target = tool.targetOf(call, state.turn)

        val shared = SharedNames.of(
            call = call,
            state = state
        )

        emit(
            value = AgentStep.Moving(
                number = number,
                move = call.name,
                target = target
            )
        )

        val key = state.tools.keyOf(call, state.turn)
        val isNew = state.made.add(key)
        if (!isNew) state.repeated += key

        // A shared name asked again counts as made again: the 4B appended to "Ideas" 29 times (the web's third run).
        if (shared != null) {
            state.found += if (isNew) shared else "(${call.name} $target $AGAIN $shared"

            return
        }

        // A change asked again is in the proposal already: the next one, or the end, is what is left.
        val isChange = tool.effect == ToolEffect.PROPOSES

        val again = if (isChange) {
            "(${call.name} $target is in the proposal: it was $AGAIN Add the next change the task asks " +
                "for, or call propose.)"
        } else {
            "(${call.name} $target $AGAIN Make a different call, with another tool.)"
        }

        if (!isNew) {
            state.found += again

            return
        }

        val before = state.turn.work.changes.size

        state.found += StepRecall.resultOf(
            call = call,
            tool = tool,
            state = state
        )

        // A change not added (refused, or held back with a question) may be asked again as it was.
        val isRefused = tool.effect == ToolEffect.PROPOSES && state.turn.work.changes.size == before
        if (isRefused) state.made.remove(element = key)

        // Refused again as it was, it counts as made again and is not offered next: refused over and over, it
        // ends the turn.
        val isRefusedAgain = isRefused && key in state.refused

        if (isRefusedAgain) {
            state.repeated += key

            state.found += "(${call.name} $target was refused the same way $AGAIN Correct what it says, or " +
                "make another call.)"
        }

        if (isRefused) state.refused += key
    }

    private suspend fun FlowCollector<AgentStep>.ended(
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ) {
        val step = when (tool.name) {
            AskUserTool.NAME -> AgentStep.Asked(
                question = call.text(name = AskUserTool.QUESTION),
                options = AskUserTool.optionsOf(call = call),
                remembering = SharedNames.rememberingOf(state = state)
            )

            ProposeTool.NAME -> proposed(
                state = state,
                summary = call.text(name = ProposeTool.SUMMARY)
            )

            else -> AgentStep.Finished(
                summary = finalAnswer.written(
                    task = state.task,
                    found = fittedOf(state = state),
                    summary = call.text(name = AnswerTool.TEXT)
                )
            )
        }

        emit(value = step)
    }

    // The changes the turn added, checked against the vault and written out for the card; the notes it read are
    // what a note it writes is written from.
    private suspend fun proposed(
        state: TurnState,
        summary: String
    ): AgentStep {
        val move = GroundedSteps.of(
            move = AgentTaskMove(
                move = ProposeTool.NAME,
                summary = summary,
                steps = state.turn.work.changes
            ),
            read = state.turn.reads.read
        )

        return proposal.stepOf(
            move = move,
            unreadable = state.turn.reads.blind,
            task = state.task,
            vault = ProposalVault(
                path = state.turn.vaultPath,
                notes = state.turn.notes,
                openNote = state.turn.openNote
            )
        )
    }

    // The next call, asked again once with more room when it cannot be read; null when it cannot twice.
    private suspend fun callOf(
        state: TurnState,
        left: Int,
        isEnding: Boolean
    ): ToolCall? {
        val first = asked(
            state = state,
            left = left,
            isEnding = isEnding,
            tokens = CALL_TOKENS
        )

        if (first != null) return first
        state.found += LoopPrompt.UNREAD

        return asked(
            state = state,
            left = left,
            isEnding = isEnding,
            tokens = RETRY_TOKENS
        )
    }

    private suspend fun asked(
        state: TurnState,
        left: Int,
        isEnding: Boolean,
        tokens: Int
    ): ToolCall? {
        val fitted = state.budget.fit(state.found, state.dropped)
        state.drops += fitted.dropped

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
            if (state.dropped == 0) state.tools.without(name = RecallTool.NAME) else state.tools

        val offered = if (isEnding) state.tools.ending() else looking

        val asking = state
            .tools
            .only(name = AskUserTool.NAME)
            .takeIf { UnsettledEnd.mustAsk(state = state) }

        val tools = asking ?: again?.let { offered.without(name = it) } ?: offered

        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = LoopPrompt.messages(
                        system = state.system,
                        task = state.task,
                        context = state.context,
                        work = state
                            .turn
                            .work
                            .described(),
                        found = fitted.entries,
                        left = left,
                        isEnding = isEnding
                    ),
                    options = GenerationOptions(
                        maxTokens = tokens,
                        temperature = 0f,
                        jsonSchema = tools.schema()
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        return ToolCallJson.read(json = json.toString())
    }

    // No call could be read, or the steps ran out: what it found is still worth an answer; with nothing, it stopped.
    private suspend fun unread(state: TurnState): AgentStep {
        val hasLooked = state.found.any {
            !it.startsWith(prefix = AgentFinalAnswer.AGAIN)
        }

        // Changes already added are what it was asked for: they go on the card, not into an answer.
        if (state.turn.work.changes.isNotEmpty()) return proposed(state, "")
        if (!hasLooked) return AgentStep.Stopped(problem = null)

        val answer = finalAnswer.written(
            task = state.task,
            found = fittedOf(state = state),
            summary = ""
        )

        return AgentStep.Finished(summary = answer)
    }

    companion object {
        const val MAX_STEPS = 30
        val TIME_LIMIT = 10.minutes
        private const val CALL_TOKENS = 700
        private const val RETRY_TOKENS = 1_400
        private const val WORK_TOKENS = 300
        private const val AGAIN = "again: its result is above."
    }
}

package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.GroundedSteps
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.agent.ProposalVault
import app.notekea.chat.domain.agent.Remembering
import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.context.ModelProfile
import app.notekea.chat.domain.context.StepMemory
import app.notekea.chat.domain.loop.OfferedTools.AGAIN
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.sources.SourceSites
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ModelJson
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.app.ShowTool
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.chat.domain.tool.work.HandTool
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText
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
    private val modelJson: ModelJson,
    private val stepMemory: StepMemory? = null,
    private val wholeLast: Int? = null,
    private val sourceSites: SourceSites? = null,
    private val frames: FrameSource? = null
) {
    private val turnEnd = TurnEnd(
        finalAnswer = AgentFinalAnswer(llmEngine = llmEngine),
        proposal = proposal,
        review = ProposalReview(
            llmEngine = llmEngine,
            modelJson = modelJson
        )
    )

    private val otherTargets = OtherTargets(
        llmEngine = llmEngine,
        modelJson = modelJson,
        vaultGuide = vaultGuide
    )

    private val router = TurnRouter(
        llmEngine = llmEngine,
        modelJson = modelJson
    )

    operator fun invoke(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        context: ActionContext,
        openNote: RelativePath?,
        scope: Set<RelativePath>? = null
    ): Flow<AgentStep> = flow {
        val remembered = vaultGuide.memoryOf(vaultPath, notes)

        val route = router.route(
            task = task,
            skills = vaultGuide.skillsOf(vaultPath, notes)
        )

        if (route.kind.isReply) return@flow emit(value = AgentStep.Replied(kind = route.kind))

        val relevant = TurnSetup.relevantOf(task, remembered, route.kind)

        // Asked of a note or a folder, the task means only notes there.
        val looked = scope?.let { only ->
            notes.filter {
                it.file.path in only
            }
        } ?: notes

        val frame =
            TurnSetup.frameOf(task, vaultPath, looked, relevant, route.kind, frames, openNote)

        // The vault's own web sources are trusted by definition (`docs/agent-web.md`): none were (bug hunt, E7).
        val turn = TurnSetup.contextOf(
            task = task,
            vaultPath = vaultPath,
            notes = notes,
            today = context.today,
            remembered = remembered,
            openNote = openNote,
            frame = frame,
            sources = sourceSites?.sitesOf(vaultPath = vaultPath).orEmpty(),
            scope = scope
        )

        val tools = StepRecall.toolsOf(
            tools = toolsets.of(
                kind = route.kind,
                task = task
            ),
            memory = stepMemory
        )

        val memory = TurnSetup.memoryOf(relevant, frame)

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
            memory = memory,
            frame = frame
        )

        val state = TurnState(
            task = task,
            context = context,
            system = system,
            tools = tools,
            turn = turn,
            memory = memory,
            frame = frame,
            kind = route.kind,
            budget = budgetOf(system, task, context)
        )

        try {
            run(state = state)
        } finally {
            withContext(context = NonCancellable) { state.budget.forget() }
        }
    }

    private suspend fun budgetOf(
        system: String,
        task: String,
        context: ActionContext
    ) = ContextBudget(
        llmEngine = llmEngine,
        tokens = ModelProfile.of(llmEngine = llmEngine).loopTokens,
        fixed = LoopPrompt.fixedOf(system, task, context),
        // The retry's room: less overflowed a small context (the bug hunt of 2026-11-17, E4).
        replyTokens = RETRY_TOKENS + WORK_TOKENS,
        memory = stepMemory,
        need = task,
        space = Random.nextLong().toString(),
        wholeLast = wholeLast
    )

    private suspend fun FlowCollector<AgentStep>.run(state: TurnState) {
        val deadline = clock.now() + TIME_LIMIT

        for (number in 1..MAX_STEPS) {
            if (clock.now() > deadline) break
            val isEnding = state.mustEnd || number == MAX_STEPS

            val call = callOf(
                state = state,
                left = MAX_STEPS - number + 1,
                isEnding = isEnding
            ) ?: return emit(value = turnEnd.unread(state) { emit(value = it) })

            val tool = state.tools[call.name]

            // Looping still on a change after it may only end: the turn ends on what it has.
            val isStopped = state.mustStop && tool?.effect != ToolEffect.ENDS
            if (isStopped) return emit(value = turnEnd.unread(state) { emit(value = it) })

            // A conclusion from search results alone: the loop reads the note first, and the model works it out again.
            val held = heldOf(
                number = number,
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

        emit(value = turnEnd.unread(state) { emit(value = it) })
    }

    // What the model is told instead of its call being made, else null: a conclusion from search results alone has
    // the note read first, by the loop.
    private suspend fun FlowCollector<AgentStep>.heldOf(
        number: Int,
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? {
        val first = SearchedUnread.readOf(
            call = call,
            state = state
        )

        if (first != null) {
            made(number, first.call, first.tool, state)

            return first.told
        }

        return TurnChecks.heldOf(
            call = call,
            state = state,
            tool = tool
        ) ?: otherTargets.heldOf(
            call = call,
            state = state
        )
    }

    // A call that reads or works: shown as a step, run once, and told when it is made again.
    private suspend fun FlowCollector<AgentStep>.made(
        number: Int,
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ) {
        val target = tool.targetOf(call, state.turn)

        // Another call is made: a tool withheld for its refusal is offered again.
        state.withheld.clear()

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
            state.found += StepRecall.againOf(key, state).takeUnless { isChange } ?: again

            return
        }

        val before = state.turn.work.changes
        state.foundAt[key] = state.found.size

        state.found += StepRecall.resultOf(
            call = call,
            tool = tool,
            state = state
        )

        // A change not added (refused, or held back with a question) may be asked again as it was.
        // The code review of 2026-11-22: a change put in an earlier one's place left the proposal's
        // size as it was, and was taken as refused.
        val isRefused = tool.effect == ToolEffect.PROPOSES && state.turn.work.changes == before
        if (isRefused) state.made.remove(element = key)

        // A refused tool is not offered until another call is made: at temperature 0 the 4B made one refused call
        // four times running (the bug hunt of 2026-11-17, E3).
        if (isRefused) state.withheld += call.name

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
    ) = emit(value = turnEnd.stepOf(call, tool, state) { emit(value = it) })

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
        val fitted = StepRecall.shownOf(state = state)

        val tools = OfferedTools.of(
            state = state,
            isEnding = isEnding
        )

        val json = try {
            ReplyText.of(
                llmEngine = llmEngine,
                messages = LoopPrompt.messages(
                    system = state.system,
                    task = state.task,
                    context = state.context,
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
        } catch (failed: LlmException) {
            state.failures += failed.error

            return null
        }

        return modelJson
            .fields(json = json)
            ?.let { ToolCall.of(fields = it) }
    }

    companion object {
        const val MAX_STEPS = 30
        val TIME_LIMIT = 10.minutes
        private const val CALL_TOKENS = 700
        private const val RETRY_TOKENS = 1_400
        private const val WORK_TOKENS = 300
    }
}

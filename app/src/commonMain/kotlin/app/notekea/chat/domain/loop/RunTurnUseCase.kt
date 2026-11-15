package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.AgentTaskMove
import app.notekea.chat.domain.agent.GroundedSteps
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.agent.ProposalVault
import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.context.ModelProfile
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolCallJson
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.change.SingularTask
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
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
    private val clock: Clock
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
        val turn = ToolContext(
            vaultPath = vaultPath,
            notes = notes,
            task = task,
            openNote = openNote,
            today = dateOf(written = context.today)
        )

        val route = router.route(
            task = task,
            skills = vaultGuide.skillsOf(vaultPath, notes)
        )

        val tools = toolsets.of(kind = route.kind)

        val system = LoopPrompt.system(
            tools = tools,
            instructions = vaultGuide.instructionsOf(vaultPath, notes),
            skill = route.skill
        )

        val state = TurnState(
            task = task,
            context = context,
            system = system,
            tools = tools,
            turn = turn,
            budget = ContextBudget(
                llmEngine = llmEngine,
                tokens = ModelProfile.of(llmEngine = llmEngine).loopTokens,
                fixed = LoopPrompt.fixedOf(system, task, context),
                replyTokens = CALL_TOKENS + WORK_TOKENS
            )
        )

        run(state = state)
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

            val unread = UnreadChoice.of(
                call = call,
                state = state
            )

            when {
                unread != null -> state.found += unread

                tool == null -> state.found += "(No tool \"${call.name}\". Call one of the tools listed.)"

                isEmptyProposal(call = call, state = state) -> state.found += NOTHING_ADDED

                isNeedlessQuestion(call = call, state = state) -> state.found += NOT_ASKED

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
        }

        emit(value = unread(state = state))
    }

    // Ended on propose with no change added, in a work that changes: told once, and the call asked again.
    private fun isEmptyProposal(
        call: ToolCall,
        state: TurnState
    ): Boolean {
        val changes = state
            .tools
            .tools
            .any {
                it.effect == ToolEffect.PROPOSES
            }

        val isEmpty =
            call.name == ProposeTool.NAME &&
                changes &&
                state.turn.work.changes.isEmpty()

        return isEmpty && state.told.add(NOTHING_ADDED)
    }

    // What the answer is written from: what was found, as far as the context goes.
    private suspend fun fittedOf(state: TurnState): List<String> = state
        .budget
        .fit(state.found, state.dropped)
        .entries

    // A question asked when the task points at nothing it leaves open (my two budgets, the Venezuelan recipes):
    // held back once, the model told to do it; asked again, it is asked.
    private fun isNeedlessQuestion(
        call: ToolCall,
        state: TurnState
    ): Boolean {
        val isOpen = SingularTask.pointsAtOne(task = state.task)
        val isNeedless = call.name == AskUserTool.NAME && !isOpen

        return isNeedless && state.told.add(NOT_ASKED)
    }

    // A call that reads or works: shown as a step, run once, and told when it is made again.
    private suspend fun FlowCollector<AgentStep>.made(
        number: Int,
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ) {
        val target = tool.targetOf(call, state.turn)

        val shared = sharedBy(
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

        if (shared != null) {
            state.found += shared

            return
        }

        val key = state.tools.keyOf(call, state.turn)
        val isNew = state.made.add(key)
        if (!isNew) state.repeated += key

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
        state.found += tool.run(call, state.turn).forModel

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

    // A bare name several notes share is not settled by the app's guess: the model is told, to ask or to say which.
    private fun sharedBy(
        call: ToolCall,
        state: TurnState
    ): String? {
        val named = listOf(call.text(name = NOTE)) + call.list(name = NOTES)

        return named
            .filter { it.isNotEmpty() }
            .firstNotNullOfOrNull { name ->
                state
                    .turn
                    .sharing(name = name)
                    .takeIf { it.isNotEmpty() }
                    ?.let { paths ->
                        "(${paths.size} notes are named \"$name\": ${paths.joinToString()}. When the task " +
                            "means one of them and does not say which, ask_user with them as the options. " +
                            "When it means them all, or says which, call again with each whole path.)"
                    }
            }
    }

    private suspend fun FlowCollector<AgentStep>.ended(
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ) {
        val step = when (tool.name) {
            AskUserTool.NAME -> AgentStep.Asked(
                question = call.text(name = AskUserTool.QUESTION),
                options = AskUserTool.optionsOf(call = call)
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

        val offered = if (isEnding) state.tools.ending() else state.tools
        val tools = again?.let { offered.without(name = it) } ?: offered

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

    private fun dateOf(written: String): CalendarDate? {
        val parts = written
            .split("-")
            .mapNotNull { it.toIntOrNull() }

        if (parts.size != DATE_PARTS) return null

        return CalendarDate(
            year = parts[0],
            month = parts[1],
            day = parts[2]
        ).takeIf { it.isValid }
    }

    companion object {
        const val MAX_STEPS = 30
        val TIME_LIMIT = 10.minutes
        private const val CALL_TOKENS = 700
        private const val RETRY_TOKENS = 1_400
        private const val WORK_TOKENS = 300
        private const val DATE_PARTS = 3
        private const val AGAIN = "again: its result is above."

        private const val NOT_ASKED =
            "(Not asked: the task names what it means, all of them, so there is nothing to choose. Do it with " +
                "what the tools found. Ask only if something it needs cannot be found.)"

        private const val NOTHING_ADDED =
            "(Nothing is added to the proposal yet, so there is nothing to confirm. Call the tool of each " +
                "change first (write_note for a note to write), then propose.)"
        private const val NOTE = "note"
        private const val NOTES = "notes"
    }
}

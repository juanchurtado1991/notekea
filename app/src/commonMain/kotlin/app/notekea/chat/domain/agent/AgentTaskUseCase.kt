package app.notekea.chat.domain.agent

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionPlanReader
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.chat.domain.tool.lookup.FindTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.lookup.SearchTool
import app.notekea.chat.domain.tool.lookup.TasksTool
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Agent mode (4.7): work that decides as it goes. The model makes one read-only move at a time and sees what it found,
 * at most [MAX_MOVES] moves in [TIME_LIMIT]; a web page only when the task wrote its address. It changes nothing: its
 * changes are 3.9's actions, checked and written out ([CheckPlanUseCase], [ComposeNotesUseCase]) for the card.
 */
class AgentTaskUseCase(
    private val llmEngine: LlmEngine,
    private val planReader: ActionPlanReader,
    private val answerFromNotes: AnswerFromNotesUseCase,
    private val noteTextCache: NoteTextCache,
    private val actionRunner: ActionRunner,
    private val pageReader: PageReader,
    private val checkPlan: CheckPlanUseCase,
    private val composeNotes: ComposeNotesUseCase,
    private val clock: Clock
) {
    private val finalAnswer = AgentFinalAnswer(llmEngine = llmEngine)

    // The look-ups a move may be, as tools (`docs/agent-loop.md`, step 1); the prompt still lists them by hand.
    private val lookUps = ToolRegistry(
        tools = listOf(
            SearchTool(answerFromNotes = answerFromNotes),
            ReadTool(noteTextCache = noteTextCache),
            FindTool(actionRunner = actionRunner),
            TasksTool(actionRunner = actionRunner),
            FetchTool(pageReader = pageReader)
        )
    )

    operator fun invoke(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        context: ActionContext,
        openNote: RelativePath?,
        planned: List<PlannedStep> = emptyList()
    ): Flow<AgentStep> = flow {
        val found = mutableListOf<String>()
        val made = mutableSetOf<String>()
        var mustEnd = false
        val deadline = clock.now() + TIME_LIMIT

        val tools = ToolContext(
            vaultPath = vaultPath,
            notes = notes,
            task = task,
            openNote = openNote
        )

        for (number in 1..MAX_MOVES) {
            if (clock.now() > deadline) break

            val move = readMove(
                task = task,
                context = context,
                found = found,
                movesLeft = MAX_MOVES - number + 1,
                mustEnd = mustEnd || number == MAX_MOVES
            )
                ?: return@flow emit(unread(task, found))

            when (move.move) {
                AgentTaskPrompt.FINISH -> return@flow finished(
                    move = move,
                    task = task,
                    found = found,
                    asked = GroundedSteps.asked(
                        planned = planned,
                        read = tools.reads.read
                    ),
                    vault = VaultAt(vaultPath, notes, openNote)
                )

                AgentTaskPrompt.PROPOSE -> return@flow proposed(
                    move = GroundedSteps.of(
                        move = move,
                        read = tools.reads.read
                    ),
                    unreadable = tools.reads.blind,
                    task = task,
                    vaultPath = vaultPath,
                    notes = notes,
                    openNote = openNote
                )

                else -> {
                    val call = move.asCall()
                    val target = lookUps[call.name]?.targetOf(call, tools).orEmpty()

                    emit(
                        value = AgentStep.Moving(
                            number = number,
                            move = move.move,
                            target = target
                        )
                    )

                    val isNew = made.add(lookUps.keyOf(call, tools))
                    mustEnd = mustEnd || !isNew

                    found += if (isNew) {
                        lookUps[call.name]?.run(call, tools)?.forModel ?: UNKNOWN_MOVE
                    } else {
                        "(${move.move} $target again: its result is above)"
                    }
                }
            }
        }

        emit(value = AgentStep.Stopped(problem = null))
    }

    // Asked to write a note, and having read what to write it from, it ends on the card ([asked]): the person
    // asked for the note and decides, not a model's doubt (the smoke of 2026-11-14). Else it ends on its answer.
    private suspend fun FlowCollector<AgentStep>.finished(
        move: AgentTaskMove,
        task: String,
        found: List<String>,
        vault: VaultAt,
        asked: AgentTaskMove?
    ) {
        if (asked != null) {
            return proposed(
                move = asked,
                unreadable = emptyList(),
                task = task,
                vaultPath = vault.path,
                notes = vault.notes,
                openNote = vault.openNote
            )
        }

        val answer = finalAnswer.written(
            task = task,
            found = found,
            summary = move.summary.orEmpty()
        )

        emit(value = AgentStep.Finished(summary = answer))
    }

    private class VaultAt(
        val path: String,
        val notes: Collection<NoteEntry>,
        val openNote: RelativePath?
    )

    // A move that cannot be read was cut short (a whole note written inside it): it is asked again, told so, with
    // more room; then the task ends on what it found, not on a failure (the developer's smoke of 2026-11-13).
    private suspend fun readMove(
        task: String,
        context: ActionContext,
        found: List<String>,
        movesLeft: Int,
        mustEnd: Boolean
    ): AgentTaskMove? = nextMove(
        task = task,
        context = context,
        found = found,
        movesLeft = movesLeft,
        mustEnd = mustEnd
    )
        ?: nextMove(
            task = task,
            context = context,
            found = found + AgentTaskPrompt.CUT_SHORT,
            movesLeft = movesLeft,
            mustEnd = mustEnd,
            tokens = RETRY_TOKENS
        )

    private suspend fun nextMove(
        task: String,
        context: ActionContext,
        found: List<String>,
        movesLeft: Int,
        mustEnd: Boolean,
        tokens: Int = MOVE_TOKENS
    ): AgentTaskMove? {
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = AgentTaskPrompt.messages(task, context, found, movesLeft, mustEnd),
                    options = GenerationOptions(
                        maxTokens = tokens,
                        temperature = 0f,
                        jsonSchema = AgentTaskPrompt.schemaOf(mustEnd = mustEnd)
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        return planReader.readTask(json = json.toString())
    }

    // No move could be read, twice: what it found is still worth an answer; with nothing found, it stopped.
    private suspend fun unread(
        task: String,
        found: List<String>
    ): AgentStep {
        val hasLooked = found.any {
            !it.startsWith(prefix = AgentFinalAnswer.AGAIN)
        }

        if (!hasLooked) return AgentStep.Stopped(problem = null)

        val answer = finalAnswer.written(
            task = task,
            found = found,
            summary = ""
        )

        return AgentStep.Finished(summary = answer)
    }

    // The proposal as the card shows it: the plan checked against the vault, its text written out.
    private suspend fun FlowCollector<AgentStep>.proposed(
        move: AgentTaskMove,
        unreadable: List<String>,
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        openNote: RelativePath?
    ) {
        // A page is clipped only from an address the task itself writes: a note or a page read on the way cannot
        // have the agent fetch an address of its own, with what it read in it (code review, 2026-11-13).
        val confirmed = UrlsNamed.shaped(
            steps = move.steps.filter { it.kind.isConfirmed },
            request = task
        )

        val summary = move.summary.orEmpty()
        if (confirmed.isEmpty()) return emit(value = AgentStep.Finished(summary = summary))

        // The notes it read to write from held no text: it says so, and writes no note from what it knows.
        val isWrittenBlind =
            unreadable.isNotEmpty() && confirmed.any { it.kind in GroundedSteps.WRITTEN }

        if (isWrittenBlind) {
            return emit(AgentStep.Stopped(ActionProblem.NothingToRead(names = unreadable)))
        }

        val checked =
            when (val plan = checkPlan(steps = confirmed, notes = notes, openNote = openNote)) {
                is Outcome.Success -> plan.value
                is Outcome.Failure -> return emit(value = AgentStep.Stopped(problem = plan.error))
            }

        val composed = checked.map { step ->
            when (
                val written =
                    composeNotes(step = step, request = task, vaultPath = vaultPath, notes = notes)
            ) {
                is Outcome.Success -> written.value
                is Outcome.Failure -> return emit(AgentStep.Stopped(problem = written.error))
            }
        }

        emit(
            value = AgentStep.Proposed(
                steps = composed,
                summary = summary
            )
        )
    }

    companion object {
        private const val UNKNOWN_MOVE = "(unknown move)"
        const val MAX_MOVES = 30
        val TIME_LIMIT = 10.minutes
        private const val MOVE_TOKENS = 900
        private const val RETRY_TOKENS = 1_600
    }
}

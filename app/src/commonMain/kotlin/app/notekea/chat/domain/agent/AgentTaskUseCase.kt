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

        val lookUp = AgentLookUp(
            answerFromNotes = answerFromNotes,
            noteTextCache = noteTextCache,
            actionRunner = actionRunner,
            pageReader = pageReader,
            vaultPath = vaultPath,
            notes = notes,
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
                ?: return@flow emit(
                    value = unread(
                        task = task,
                        found = found
                    )
                )

            when (move.move) {
                AgentTaskPrompt.FINISH -> {
                    // Asked to write a note, and having read what to write it from, it ends on the card: the
                    // person asked for the note and decides, not a model's doubt (the smoke of 2026-11-14).
                    val asked = GroundedSteps.asked(
                        planned = planned,
                        read = lookUp.read
                    )

                    if (asked != null) {
                        return@flow proposed(
                            move = asked,
                            unreadable = emptyList(),
                            task = task,
                            vaultPath = vaultPath,
                            notes = notes,
                            openNote = openNote
                        )
                    }

                    val answer = finalAnswer.written(
                        task = task,
                        found = found,
                        summary = move.summary.orEmpty()
                    )

                    return@flow emit(value = AgentStep.Finished(summary = answer))
                }

                AgentTaskPrompt.PROPOSE -> return@flow proposed(
                    move = GroundedSteps.of(
                        move = move,
                        read = lookUp.read
                    ),
                    unreadable = lookUp
                        .unreadable
                        .takeIf { lookUp.read.isEmpty() }
                        .orEmpty(),
                    task = task,
                    vaultPath = vaultPath,
                    notes = notes,
                    openNote = openNote
                )

                else -> {
                    // A note is the same note however the model writes its name ("contrato", "Contrato/contrato").
                    val note = move.note?.let {
                        lookUp.pathOf(name = it) ?: it
                    }

                    val key = "${move.move}:${move.query ?: note ?: move.url}".lowercase()

                    emit(
                        value = AgentStep.Moving(
                            number = number,
                            move = move.move,
                            target = targetOf(
                                move = move,
                                lookUp = lookUp
                            )
                        )
                    )

                    val isNew = made.add(key)
                    mustEnd = mustEnd || !isNew

                    found += if (isNew) {
                        lookUp.result(
                            move = move,
                            task = task
                        )
                    } else {
                        "(${move.move} ${targetOf(move, lookUp)} again: its result is above)"
                    }
                }
            }
        }

        emit(value = AgentStep.Stopped(problem = null))
    }

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

    // A note read is listed by its path in the vault ("Read Recipes/Arepas"), however the model named it.
    private fun targetOf(
        move: AgentTaskMove,
        lookUp: AgentLookUp
    ): String {
        val note = move.note?.let {
            lookUp.pathOf(name = it) ?: it
        }

        return move.query ?: note ?: move.url ?: ""
    }

    companion object {
        const val MAX_MOVES = 30
        val TIME_LIMIT = 10.minutes
        private const val MOVE_TOKENS = 900
        private const val RETRY_TOKENS = 1_600
    }
}

package app.notekea.chat.domain

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
    operator fun invoke(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        context: ActionContext,
        openNote: RelativePath?
    ): Flow<AgentStep> = flow {
        val found = mutableListOf<String>()
        val made = mutableSetOf<String>()
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

            val move = nextMove(
                task = task,
                context = context,
                found = found,
                movesLeft = MAX_MOVES - number + 1
            )
                ?: return@flow emit(value = AgentStep.Stopped(problem = null))

            when (move.move) {
                AgentTaskPrompt.FINISH -> {
                    return@flow emit(value = AgentStep.Finished(summary = move.summary.orEmpty()))
                }

                AgentTaskPrompt.PROPOSE -> return@flow proposed(
                    move = move,
                    task = task,
                    vaultPath = vaultPath,
                    notes = notes,
                    openNote = openNote
                )

                else -> {
                    val key = "${move.move}:${move.query ?: move.note ?: move.url}".lowercase()

                    emit(
                        value = AgentStep.Moving(
                            number = number,
                            move = move.move,
                            target = targetOf(move = move)
                        )
                    )

                    found += if (made.add(key)) {
                        lookUp.result(
                            move = move,
                            task = task
                        )
                    } else {
                        "(${move.move} ${targetOf(move)}: already done, try another move or end the task)"
                    }
                }
            }
        }

        emit(value = AgentStep.Stopped(problem = null))
    }

    private suspend fun nextMove(
        task: String,
        context: ActionContext,
        found: List<String>,
        movesLeft: Int
    ): AgentTaskMove? {
        val json = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = AgentTaskPrompt.messages(task, context, found, movesLeft),
                    options = GenerationOptions(
                        maxTokens = MOVE_TOKENS,
                        temperature = 0f,
                        jsonSchema = AgentTaskPrompt.SCHEMA
                    )
                )
                .collect { json.append(it) }
        } catch (_: LlmException) {
            return null
        }

        return planReader.readTask(json = json.toString())
    }

    // The proposal as the card shows it: the plan checked against the vault, its text written out.
    private suspend fun FlowCollector<AgentStep>.proposed(
        move: AgentTaskMove,
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        openNote: RelativePath?
    ) {
        val confirmed = move.steps.filter { it.kind.isConfirmed }
        val summary = move.summary.orEmpty()
        if (confirmed.isEmpty()) return emit(value = AgentStep.Finished(summary = summary))

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

    private fun targetOf(move: AgentTaskMove): String = move.query ?: move.note ?: move.url ?: ""

    companion object {
        const val MAX_MOVES = 30
        val TIME_LIMIT = 10.minutes
        private const val MOVE_TOKENS = 900
    }
}

package app.notekea.chat.eval

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuide
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.chat.domain.sources.AnswerFromPagesUseCase
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.viewmodel.ChatSources
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.search.eval.EvalVault
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Clock

/**
 * A task worked on by the chat itself in Fast mode, as a person who chose Fast would see it: the plan of 3.9 and the
 * answers from the notes, no agent. Its last reply is read as the agent's end ([AgentStep]), so the blind pool's checks
 * measure Fast and Agent alike (the developer's question of 2026-11-26: is the agent the one mode to keep?).
 */
internal class FastRun(
    private val indexed: EvalVault,
    private val notes: List<NoteEntry>,
    private val engine: LlmEngine,
    private val embedder: TextEmbedder,
    private val runner: ActionRunner
) {
    private val cache = NoteTextCache(contentStore = indexed.store)
    private val answer = AnswerFromNotesUseCase(embedder, indexed.index, cache, engine)
    private val guide = HelpGuide.of(markdown = File(GUIDE).readText())

    suspend fun of(task: String): List<AgentStep> {
        val chat = chatOf()

        chat.onIntent(intent = ChatIntent.OnShown)
        chat.onIntent(intent = ChatIntent.OnNotesChange(notes = notes))
        chat.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        chat.onIntent(intent = ChatIntent.OnModeChoose(mode = ChatMode.FAST))
        settled(chat = chat)

        chat.onIntent(intent = ChatIntent.OnQuestionChange(text = task))
        chat.onIntent(intent = ChatIntent.OnSend)
        settled(chat = chat)

        val entry = chat
            .state
            .value
            .entries
            .lastOrNull() ?: return listOf(AgentStep.Stopped(problem = null))

        return listOf(endOf(entry = entry))
    }

    private suspend fun settled(chat: ChatViewModel) {
        delay(timeMillis = SETTLE_MILLIS)
        while (chat.state.value.isAnswering) delay(timeMillis = SETTLE_MILLIS)
    }

    private fun chatOf(): ChatViewModel {
        val compose = ComposeNotesUseCase(engine, cache, GhostActionPlanReader())

        return ChatViewModel(
            vaultPath = indexed.path,
            answerFromNotes = answer,
            converse = ConverseUseCase(engine, cache),
            agentAnswer = AgentAnswerUseCase(answer, cache, engine, GhostActionPlanReader()),
            answerHelp = AnswerHelpUseCase(
                llmEngine = engine,
                guideSource = { guide }
            ),
            describeVault = DescribeVaultUseCase(llmEngine = engine),
            conventions = PreviewPlatformConventions.linux,
            actOnRequest = ActOnRequestUseCase(
                PlanRequestUseCase(engine, GhostActionPlanReader()),
                CheckPlanUseCase(),
                compose,
                runner
            ),
            agentTask = AgentTaskUseCase(
                llmEngine = engine,
                planReader = GhostActionPlanReader(),
                answerFromNotes = answer,
                noteTextCache = cache,
                actionRunner = runner,
                pageReader = { Outcome.Failure(error = PageProblem.OFFLINE) },
                checkPlan = CheckPlanUseCase(),
                composeNotes = compose,
                clock = Clock.System
            ),
            compactChat = CompactChatUseCase(llmEngine = engine),
            answerFromFound = AnswerFromFoundUseCase(llmEngine = engine),
            actionRunner = runner,
            localCalendar = JvmLocalCalendar(),
            clock = Clock.System,
            historyStore = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard(),
            llmEngine = engine,
            sources = ChatSources(
                answerFromPages = AnswerFromPagesUseCase(llmEngine = engine),
                sourceSites = null
            )
        )
    }

    // The reply as the agent would have ended: a question back, a card of changes, a problem, or what it said and
    // found (a table's rows are what it answered with when it answered in none).
    private fun endOf(entry: ChatEntry): AgentStep {
        val action = entry.action

        val found = action
            ?.found
            .orEmpty()
            .flatMap { table ->
                table.rows.map { row -> "${row.note.bare()} ${row.cells.joinToString(separator = " ")}" }
            }

        return when {
            entry.options.isNotEmpty() -> AgentStep.Asked(
                question = entry.answer,
                options = entry.options
            )

            action != null && action.steps.isNotEmpty() -> AgentStep.Proposed(
                steps = action.steps,
                summary = entry.answer
            )

            action?.problem != null -> AgentStep.Stopped(problem = action.problem)

            action != null && action.shown.isNotEmpty() -> AgentStep.Shown(steps = action.shown)

            else -> AgentStep.Finished(
                summary = (listOf(entry.answer) + found).joinToString(separator = "\n")
            )
        }
    }

    private companion object {
        const val SETTLE_MILLIS = 300L
        const val GUIDE = "src/commonMain/composeResources/files/guide/guide.md"
    }
}

package app.notekea.chat.presentation

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuide
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import kotlin.time.Clock

/** A [ChatViewModel] of the real use cases over fakes, as the chat's tests build it; [guide] is the app's guide. */
internal fun chatViewModel(
    engine: FakeLlmEngine,
    store: FakeNoteContentStore,
    index: FakeVectorIndex,
    embedder: FakeTextEmbedder,
    runner: FakeActionRunner,
    history: ChatHistoryStore,
    clipboard: FakeTextClipboard,
    guide: HelpGuide = HelpGuide(topics = emptyList()),
    clock: Clock = Clock.System,
    clipper: WebPageClipper? = null
): ChatViewModel {
    val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    val answer = AnswerFromNotesUseCase(
        textEmbedder = embedder,
        vectorIndex = index,
        noteTextCache = cache,
        llmEngine = engine
    )

    return ChatViewModel(
        vaultPath = VAULT,
        answerFromNotes = answer,
        converse = ConverseUseCase(
            llmEngine = engine,
            noteTextCache = cache
        ),
        agentAnswer = AgentAnswerUseCase(
            answerFromNotes = answer,
            noteTextCache = cache,
            llmEngine = engine,
            planReader = GhostActionPlanReader()
        ),
        answerHelp = AnswerHelpUseCase(
            llmEngine = engine,
            guideSource = { guide }
        ),
        describeVault = DescribeVaultUseCase(llmEngine = engine),
        conventions = PreviewPlatformConventions.linux,
        actOnRequest = ActOnRequestUseCase(
            planRequest = PlanRequestUseCase(
                llmEngine = engine,
                planReader = GhostActionPlanReader()
            ),
            checkPlan = CheckPlanUseCase(),
            composeNotes = ComposeNotesUseCase(
                llmEngine = engine,
                noteTextCache = cache,
                planReader = GhostActionPlanReader(),
                clipper = clipper
            ),
            actionRunner = runner
        ),
        agentTask = AgentTaskUseCase(
            llmEngine = engine,
            planReader = GhostActionPlanReader(),
            answerFromNotes = answer,
            noteTextCache = cache,
            actionRunner = runner,
            pageReader = { Outcome.Failure(error = PageProblem.OFFLINE) },
            checkPlan = CheckPlanUseCase(),
            composeNotes = ComposeNotesUseCase(
                llmEngine = engine,
                noteTextCache = cache,
                planReader = GhostActionPlanReader()
            ),
            clock = Clock.System
        ),
        compactChat = CompactChatUseCase(llmEngine = engine),
        answerFromFound = AnswerFromFoundUseCase(llmEngine = engine),
        actionRunner = runner,
        localCalendar = FakeLocalCalendar(),
        clock = clock,
        historyStore = history,
        clipboard = clipboard,
        llmEngine = engine
    )
}

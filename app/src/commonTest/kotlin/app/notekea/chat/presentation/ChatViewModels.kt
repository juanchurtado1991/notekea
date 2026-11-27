package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatHistoryStore
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.answer.DescribeVaultUseCase
import app.notekea.chat.domain.answer.PassageSearch
import app.notekea.chat.domain.help.AnswerHelpUseCase
import app.notekea.chat.domain.help.HelpGuide
import app.notekea.chat.domain.loop.ChatLoop
import app.notekea.chat.domain.loop.LoopTools
import app.notekea.chat.domain.loop.Toolsets
import app.notekea.chat.domain.loop.loopTurn
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.chat.domain.sources.AnswerFromPagesUseCase
import app.notekea.chat.domain.sources.SourceSites
import app.notekea.chat.presentation.viewmodel.ChatSources
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.web.domain.WebSearchProblem
import kotlin.time.Clock

/**
 * A [ChatViewModel] of the real use cases over fakes, as the chat's tests build it; [guide] is the app's guide. A
 * request of the whole vault goes to the real loop, over the app's toolsets unless a test passes its own [toolsets].
 */
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
    clipper: WebPageClipper? = null,
    sourceSites: SourceSites? = null,
    toolsets: Toolsets? = null,
    preferences: AppPreferencesStore = FakeAppPreferencesStore()
): ChatViewModel {
    val cache = NoteTextCache(contentStore = store)

    val passages = PassageSearch(
        textEmbedder = embedder,
        vectorIndex = index,
        noteTextCache = cache,
        llmEngine = engine
    )

    val tools = toolsets ?: LoopTools(
        llmEngine = engine,
        passageSearch = passages,
        noteTextCache = cache,
        actionRunner = runner,
        pageReader = { Outcome.Failure(error = PageProblem.OFFLINE) },
        linkFinder = JetBrainsMarkdownLinkFinder(),
        webSearch = { _, _ -> Outcome.Failure(error = WebSearchProblem.OFFLINE) }
    ).toolsets()

    return ChatViewModel(
        vaultPath = VAULT,
        converse = ConverseUseCase(
            llmEngine = engine,
            noteTextCache = cache
        ),
        answerHelp = AnswerHelpUseCase(
            llmEngine = engine,
            guideSource = { guide }
        ),
        describeVault = DescribeVaultUseCase(llmEngine = engine),
        conventions = PreviewPlatformConventions.linux,
        compactChat = CompactChatUseCase(llmEngine = engine),
        actionRunner = runner,
        localCalendar = FakeLocalCalendar(),
        clock = clock,
        historyStore = history,
        clipboard = clipboard,
        llmEngine = engine,
        sources = ChatSources(
            answerFromPages = AnswerFromPagesUseCase(llmEngine = engine),
            sourceSites = sourceSites
        ),
        loop = ChatLoop(
            runTurn = loopTurn(
                engine = engine,
                cache = cache,
                toolsets = tools,
                clipper = clipper
            ),
            preferencesStore = preferences
        )
    )
}

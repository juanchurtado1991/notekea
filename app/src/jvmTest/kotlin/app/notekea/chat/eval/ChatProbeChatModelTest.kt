package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
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
import app.notekea.chat.domain.sources.AnswerFromPagesUseCase
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.viewmodel.ChatSources
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.data.LuceneVectorIndex
import app.notekea.core.notes.data.NioNoteFileAttributes
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioNoteContentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.data.OkioNoteScanner
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.UpdateLinksAfterRenameUseCase
import app.notekea.core.notes.domain.templates.FillTemplateUseCase
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.search.domain.FakeIndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.search.domain.PlanIndexUseCase
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.shell.data.chat.VaultActionRunner
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.data.OkioNoteMetadataReader
import app.notekea.tree.data.VaultFolderTrash
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.FakeBookmarkStore
import app.notekea.tree.domain.OpenDailyNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Clock

/**
 * The chat as a person uses it: the app's [ChatViewModel] over the real models and a copy of the Sample Vault, fed the
 * conversations of `smoke-data/eval/chat-probes.txt` (`@mode`, `@open`, `@confirm` as directions) and written out as
 * Copy chat writes them, with the plans, to `build/reports/chat-probe-<size>.md`. A tool to read, not a pass or fail.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatProbeChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.MEDIUM
    private val dispatchers = JvmAppDispatchers()
    private val natives = Files.createTempDirectory("notekea-natives").toString()
    private val fileSystem = FileSystem.SYSTEM
    private val main = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val embedder = LlamaTextEmbedder(
        modelPath = System.getProperty("notekea.embedding.model").orEmpty(),
        nativeFolder = natives
    )

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = GPU_SLOTS,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = natives,
        dispatchers = dispatchers
    )

    // The plans the model wrote for each conversation, written under it in the report to tell why a reply went wrong.
    private val plans = mutableListOf<String>()

    private val planning = object : LlmEngine by engine {
        override fun generate(
            messages: List<ChatMessage>,
            options: GenerationOptions
        ): Flow<String> = flow {
            val plan = StringBuilder()

            engine
                .generate(messages, options)
                .collect {
                    plan.append(it)
                    emit(value = it)
                }

            plans += plan.toString()
        }
    }

    @AfterTest
    fun tearDown() {
        engine.close()
        embedder.close()
        Dispatchers.resetMain()
        main.close()
    }

    @Test
    fun `the probe conversations are written out as the chat answers them`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))
        Dispatchers.setMain(dispatcher = main)

        val probes = File(
            System.getProperty(PROBES_PROPERTY)?.takeIf { it.isNotBlank() } ?: DEFAULT_PROBES
        )
            .readText()
            .split(Regex(pattern = "(?m)^---\\s*$"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val report = StringBuilder("# Chat probe (${size.id})\n")

        probes.forEachIndexed { index, probe ->
            val vault = CommittedVault.copy()

            val lines = probe
                .lines()
                .map { it.trim() }
                .filter {
                    it.isNotEmpty() && !it.startsWith("#")
                }

            val earlier = lines
                .firstOrNull { it.startsWith(HISTORY) }
                ?.removePrefix(HISTORY)
                ?.trim()
                ?.toInt() ?: 0

            val chat = chatOf(
                vault = vault,
                earlier = earlier
            )

            val start = System.nanoTime()

            lines.filterNot { it.startsWith(HISTORY) }.forEach { line ->
                play(
                    chat = chat,
                    line = line
                )
            }

            val seconds = (System.nanoTime() - start) / NANOS_PER_SECOND

            chat.second.onIntent(intent = ChatIntent.OnCopyChat)
            awaitIdle(chat = chat.second)

            report.append("\n\n<!-- probe ${index + 1}, $seconds s -->\n")

            report.append(
                chat
                    .third
                    .copied
                    .lastOrNull()
                    .orEmpty()
            )

            report.append("\n<!-- ${memoryOf(chat = chat.second)} -->")
            plans.forEach { report.append("\n<!-- plan: $it -->") }
            plans.clear()
            vault.deleteRecursively()
        }

        File("build/reports/chat-probe-${size.id}.md").run {
            parentFile.mkdirs()
            writeText(text = report.toString())
        }
    }

    private suspend fun play(
        chat: Triple<File, ChatViewModel, FakeTextClipboard>,
        line: String
    ) {
        val viewModel = chat.second

        when {
            line.startsWith(MODE) -> viewModel.onIntent(
                intent = ChatIntent.OnModeChoose(
                    mode = ChatMode.valueOf(
                        line
                            .removePrefix(MODE)
                            .trim()
                            .uppercase()
                    )
                )
            )

            line.startsWith(OPEN) -> viewModel.onIntent(
                intent = ChatIntent.OnActiveNoteChange(
                    path = RelativePath(value = line.removePrefix(OPEN).trim())
                )
            )

            line == CONFIRM ->
                viewModel
                    .state
                    .value
                    .entries
                    .lastOrNull()
                    ?.let { viewModel.onIntent(intent = ChatIntent.OnActionConfirm(id = it.id)) }

            else -> {
                viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = line))
                viewModel.onIntent(intent = ChatIntent.OnSend)
            }
        }

        awaitIdle(chat = viewModel)
    }

    private suspend fun awaitIdle(chat: ChatViewModel) {
        delay(timeMillis = SETTLE_MILLIS)
        while (chat.state.value.isAnswering) delay(timeMillis = SETTLE_MILLIS)
        delay(timeMillis = SETTLE_MILLIS)
    }

    /** The chat's memory once a compaction under way ends, or after [COMPACT_SECONDS] if it never does. */
    private suspend fun memoryOf(chat: ChatViewModel): String {
        val start = System.nanoTime()
        val limit = COMPACT_SECONDS * NANOS_PER_SECOND

        while (chat.state.value.isCompacting) {
            val isLate = System.nanoTime() - start >= limit
            if (isLate) break
            delay(timeMillis = SETTLE_MILLIS)
        }

        val state = chat.state.value
        val waited = (System.nanoTime() - start) / NANOS_PER_SECOND

        return "memory: ${(state.contextUsage * PERCENT).toInt()} %, compacting: ${state.isCompacting}, waited $waited s"
    }

    private suspend fun chatOf(
        vault: File,
        earlier: Int
    ): Triple<File, ChatViewModel, FakeTextClipboard> {
        val store = OkioNoteContentStore(fileSystem, dispatchers, NioNoteFileAttributes())
        val scanner = OkioNoteScanner(fileSystem, dispatchers)
        val cache = NoteTextCache(store, TextFolder())

        val notes = notesOf(
            vault = vault,
            scanner = scanner
        )

        val indexFolder = Files.createTempDirectory("notekea-probe-index").toString()
        val index = LuceneVectorIndex(indexFolder, dispatchers)
        val sections = NoteSections(outlineFinder = OutlineFinder())

        IndexVaultUseCase(
            planIndex = PlanIndexUseCase(),
            chunkNote = ChunkNoteUseCase(noteSections = sections),
            noteContentStore = store,
            textEmbedder = embedder,
            vectorIndex = index,
            indexStateStore = FakeIndexStateStore()
        )(
            vaultPath = vault.path,
            notes = notes.map { it.file }
        ).last()

        val clipboard = FakeTextClipboard()

        val viewModel = viewModelOf(
            vault = vault,
            runner = ProbeRunner.of(
                store = store,
                scanner = scanner,
                cache = cache,
                engine = engine,
                fileSystem = fileSystem,
                dispatchers = dispatchers
            ),
            answer = AnswerFromNotesUseCase(embedder, index, cache, engine),
            cache = cache,
            clipboard = clipboard,
            history = CommittedVault.earlierChat(
                vault = vault,
                earlier = earlier
            )
        )

        viewModel.onIntent(intent = ChatIntent.OnShown)
        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = notes))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        awaitIdle(chat = viewModel)

        return Triple(vault, viewModel, clipboard)
    }

    private fun viewModelOf(
        vault: File,
        runner: VaultActionRunner,
        answer: AnswerFromNotesUseCase,
        cache: NoteTextCache,
        clipboard: FakeTextClipboard,
        history: FakeChatHistoryStore
    ): ChatViewModel {
        val compose = ComposeNotesUseCase(engine, cache, GhostActionPlanReader())
        val guide = HelpGuide.of(markdown = File(GUIDE).readText())

        return ChatViewModel(
            vaultPath = vault.path,
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
                PlanRequestUseCase(planning, GhostActionPlanReader()),
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
            historyStore = history,
            clipboard = clipboard,
            llmEngine = engine,
            sources = ChatSources(
                answerFromPages = AnswerFromPagesUseCase(llmEngine = engine),
                sourceSites = null
            )
        )
    }

    private suspend fun notesOf(
        vault: File,
        scanner: OkioNoteScanner
    ): List<NoteEntry> {
        val listing = (scanner.scan(vaultPath = vault.path) as Outcome.Success).value
        val reader = OkioNoteMetadataReader(fileSystem, dispatchers, GhostFrontmatterParser())

        return listing.files.map {
            NoteEntry(it, reader.read(vault.path, it.path) ?: NoteMetadata.NONE)
        }
    }

    private companion object {
        const val GPU_SLOTS = 4
        const val SETTLE_MILLIS = 300L
        const val NANOS_PER_SECOND = 1_000_000_000L
        const val MODE = "@mode"
        const val OPEN = "@open"
        const val CONFIRM = "@confirm"
        const val HISTORY = "@history"
        const val COMPACT_SECONDS = 120L
        const val PERCENT = 100
        const val PROBES_PROPERTY = "notekea.chat.probes"
        const val DEFAULT_PROBES = "../smoke-data/eval/chat-probes.txt"
        const val GUIDE = "src/commonMain/composeResources/files/guide/guide.md"

        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}

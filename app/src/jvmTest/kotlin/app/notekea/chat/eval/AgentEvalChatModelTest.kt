package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.eval.blind.BlindExams
import app.notekea.chat.eval.blind.BlindPool
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.search.eval.EvalVault
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.data.OkioNoteMetadataReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Agent mode's evaluation (4.7): every task of [AgentEvalCases] worked on the committed Sample Vault with the real
 * models, its moves and end written to `build/reports/agent-eval-<size>.md`. A size is offered agent mode only when
 * it reaches [OFFER_BAR]. Run by hand like [AnswerEvalChatModelTest], with `--tests '*AgentEval*'`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AgentEvalChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.MEDIUM
    private val dispatchers = JvmAppDispatchers()
    private val natives = Files.createTempDirectory("notekea-natives").toString()
    private val embedder = LlamaTextEmbedder(
        modelPath = System.getProperty("notekea.embedding.model").orEmpty(),
        nativeFolder = natives
    )

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(EvalModels.FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = GPU_SLOTS,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val llama = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = natives,
        dispatchers = dispatchers
    )

    // The model, its replies kept on disk when a round asks for it (`ReplyCache`).
    private val engine = ReplyCache.around(
        engine = llama,
        modelPath = setup.modelPath
    )

    // Every call of each task, by its task, for the report of a task that went wrong.
    private val trace = EvalTrace(engine = engine)
    private val recorded = trace.recorded

    // The chat's own thread, for [FastRun]: its view model works on the main dispatcher.
    private val main = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        main.close()
        (engine as? ReplyCache)?.note()
        llama.close()
        embedder.close()
    }

    @Test
    fun `agent mode does the tasks of the evaluation at or above the bar to offer it`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

            val (share, report) = measured(
                vault = VAULT,
                all = AgentEvalCases.all,
                name = "agent-eval"
            )

            assertTrue(
                actual = share >= OFFER_BAR,
                message = report
            )
        }

    // The wider tasks of docs/agent-loop.md, step 0: today's agent on them is the mark the one loop must pass.
    @Test
    fun `agent mode is measured on documents, long notes and requests read two ways`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

            measured(
                vault = AgentWideCases.VAULT,
                all = AgentWideCases.all.map { it.forToday() },
                name = "agent-wide"
            )

            Unit
        }

    // The held-out tasks (docs/agent-loop.md, step 5): never looked at while tuning the loop, so they say whether
    // what was tuned holds on tasks it was not tuned on. Today's agent and the loop are measured on the same ones.
    @Test
    fun `today's agent is measured on the held-out tasks`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(VAULT, AgentHeldOutCases.all, "agent-heldout")

        measured(
            AgentWideCases.VAULT,
            AgentWideHeldOutCases.all.map { it.forToday() },
            "agent-wide-heldout"
        )

        Unit
    }

    @Test
    fun `the one loop is measured on the held-out tasks`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(VAULT, AgentHeldOutCases.all, "loop-heldout", isLoop = true)

        measured(
            vault = AgentWideCases.VAULT,
            all = AgentWideHeldOutCases.all.map { it.forLoop() },
            name = "loop-wide-heldout",
            isLoop = true
        )

        Unit
    }

    // Tasks the verifier's checks were not written against: whether asking back holds beyond the tuned tasks.
    @Test
    fun `the one loop is measured on tasks its checks were not written against`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))
        val all = AgentUnseenCases.all.map { it.forLoop() }
        measured(AgentWideCases.VAULT, all, "loop-unseen", isLoop = true)
        Unit
    }

    // docs/agent-memory.md: the unseen tasks with a memory that settles them, and the memory's other uses.
    @Test
    fun `the one loop is measured with the chat's memory`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))
        val vault = AgentWideCases.VAULT
        val settling = AgentMemoryCases.SETTLING

        val clear = AgentUnseenCases.all.filter {
            it.ends != AgentWideCase.End.ASK
        }

        val all = (AgentMemoryCases.settled + clear + AgentMemoryCases.uses).map { it.forLoop() }

        measured(
            vault,
            all,
            "loop-memory",
            isLoop = true,
            written = mapOf(MEMORY to settling)
        )

        val wrong = AgentMemoryCases.followed.map { it.forLoop() }

        measured(
            vault,
            wrong,
            "loop-memory-wrong",
            isLoop = true,
            written = mapOf(MEMORY to AgentMemoryCases.WRONG)
        )

        measured(
            vault,
            AgentMemoryCases.poisoning,
            "loop-memory-poisoned",
            isLoop = true,
            written = AgentMemoryCases.POISONED
        )

        Unit
    }

    // The one loop on agent mode's 30 tasks: it may not do worse than today's agent to take its place (step 6).
    @Test
    fun `the one loop is measured on the tasks of agent mode`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(
            vault = VAULT,
            all = AgentEvalCases.all,
            name = "loop-eval",
            isLoop = true
        )

        Unit
    }

    // The one loop on the same wider tasks (docs/agent-loop.md, step 5), asking where today's agent cannot.
    @Test
    fun `the one loop is measured on documents, long notes and requests read two ways`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

            measured(
                vault = AgentWideCases.VAULT,
                all = AgentWideCases.all.map { it.forLoop() },
                name = "loop-wide",
                isLoop = true
            )

            Unit
        }

    // docs/eval-fresh.md: a vault and tasks written blind, to tell whether the loop generalises.
    @Test
    fun `the one loop is measured on a fresh vault it was never tuned on`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(
            vault = AgentFreshCases.VAULT,
            all = AgentFreshCases.all.map { it.forLoop() },
            name = "loop-fresh",
            isLoop = true
        )

        Unit
    }

    // docs/eval-blind.md: nine more vaults written blind, measured before and after a change to the loop only.
    @Test
    fun `blind exams on nine vaults the loop was never tuned on`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        BlindExams.all.forEach { exam ->
            measured(exam.vault, exam.cases.map { it.forLoop() }, "blind-${exam.id}", isLoop = true)
        }
    }

    // docs/eval-blind.md: ten exams of the blind pool picked at random by a seed, all 40 unsealed, or the ten sealed.
    @Test
    fun `blind pool, ten exams the loop was never tuned on`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val seed = System
            .getProperty("notekea.chat.poolSeed")
            .orEmpty()
            .toLongOrNull() ?: 1

        val exams = when (System.getProperty("notekea.chat.pool")) {
            "sealed" -> BlindPool.sealed
            "all" -> BlindPool.unsealed
            else -> BlindPool.sampleOf(seed = seed)
        }

        // -PchatFast=true: the same exams worked on by the chat in Fast mode ([FastRun]), the agent's checks kept.
        val isFast = System.getProperty("notekea.chat.fast") == "true"
        if (isFast) Dispatchers.setMain(dispatcher = main)

        exams.forEach { exam ->
            measured(
                vault = exam.vault,
                all = exam.cases.map { it.forLoop() },
                name = "pool-${exam.id}",
                isLoop = !isFast,
                isFast = isFast
            )
        }
    }

    /** The share of [cases] done right on [vault], and the report written to `build/reports/[name]-<size>.md`. */
    private suspend fun measured(
        vault: String,
        all: List<AgentEvalCase>,
        name: String,
        isLoop: Boolean = false,
        written: Map<String, String> = emptyMap(),
        isFast: Boolean = false
    ): Pair<Double, String> {
        val indexed = EvalVault.of(
            vault = vault,
            embedder = embedder,
            dispatchers = dispatchers
        )

        // Only the tasks a round is narrowed to, by words or by a list (`EvalTasks`).
        val picked = EvalTasks.fromProperties()
        val cases = all.filter { picked.holds(task = it.task) }

        if (cases.isEmpty()) return 1.0 to ""

        val notes = AgentEvalSetup.withWritten(
            indexed = indexed,
            notes = AgentEvalSetup.withMetadata(indexed, dispatchers),
            written = written
        )

        val agent = agentOf(indexed = indexed)

        val cache = NoteTextCache(contentStore = indexed.store)
        val runner = AgentEvalSetup.runnerOf(indexed, cache, engine)

        val loop = LoopEval.loopOf(
            engine = recorded,
            embedder = embedder,
            indexed = indexed,
            runner = runner,
            trace = trace
        )

        val fast = FastRun(indexed, notes, recorded, embedder, runner)

        val slots = Semaphore(permits = setup.slots)

        val results = coroutineScope {
            cases
                .map { case ->
                    async {
                        slots.withPermit {
                            val start = System.nanoTime()

                            val context = AgentEvalSetup.contextOf(notes = notes)

                            val worked = when {
                                isFast -> null
                                isLoop -> loop(case.task, indexed.path, notes, context, null)
                                else -> agent(case.task, indexed.path, notes, context, null)
                            }

                            val steps = worked?.toList() ?: fast.of(task = case.task)

                            val seconds = (System.nanoTime() - start) / NANOS_PER_SECOND

                            AgentResult(case, steps, seconds, case.check(steps.last()))
                        }
                    }
                }
                .awaitAll()
        }

        val right = results.count {
            it.problem == null
        }

        val report = AgentEvalReport(
            trace = trace,
            size = size
        ).of(
            vault = vault,
            results = results,
            right = right
        )

        File("build/reports/$name-${size.id}.md").run {
            parentFile.mkdirs()
            writeText(text = report)
        }

        println(
            report
                .lines()
                .take(n = 3)
                .joinToString(separator = "\n")
        )

        return right.toDouble() / results.size to report
    }

    private fun agentOf(indexed: EvalVault): AgentTaskUseCase {
        val cache = NoteTextCache(contentStore = indexed.store)

        val runner = AgentEvalSetup.runnerOf(indexed, cache, engine)

        return AgentTaskUseCase(
            llmEngine = recorded,
            planReader = GhostActionPlanReader(),
            answerFromNotes = AnswerFromNotesUseCase(embedder, indexed.index, cache, engine),
            noteTextCache = cache,
            actionRunner = runner,
            pageReader = { Outcome.Failure(error = PageProblem.OFFLINE) },
            checkPlan = CheckPlanUseCase(),
            composeNotes = ComposeNotesUseCase(engine, cache, GhostActionPlanReader()),
            clock = Clock.System
        )
    }

    private companion object {
        const val VAULT = "Sample Vault"
        val MEMORY = VaultGuide.MEMORY.value
        const val GPU_SLOTS = 4
        const val NANOS_PER_SECOND = 1_000_000_000L

        // A size is offered agent mode when four tasks in five come out right.
        const val OFFER_BAR = 0.8
    }
}

package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.eval.blind.BlindExams
import app.notekea.chat.eval.blind.BlindPool
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.search.eval.EvalVault
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * The agent's evaluation (4.7): the tasks of each set worked by the one loop on its vault with the real models, its
 * moves and end written to `build/reports/<name>-<size>.md`. Run by hand, with `--tests '*AgentEval*'`.
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

    @AfterTest
    fun tearDown() {
        (engine as? ReplyCache)?.note()
        llama.close()
        embedder.close()
    }

    @Test
    fun `the one loop is measured on the held-out tasks`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(VAULT, AgentHeldOutCases.all, "loop-heldout")

        measured(
            vault = AgentWideCases.VAULT,
            all = AgentWideHeldOutCases.all.map { it.forLoop() },
            name = "loop-wide-heldout"
        )
    }

    // Tasks the verifier's checks were not written against: whether asking back holds beyond the tuned tasks.
    @Test
    fun `the one loop is measured on tasks its checks were not written against`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))
        val all = AgentUnseenCases.all.map { it.forLoop() }
        measured(AgentWideCases.VAULT, all, "loop-unseen")
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
            written = mapOf(MEMORY to settling)
        )

        val wrong = AgentMemoryCases.followed.map { it.forLoop() }

        measured(
            vault,
            wrong,
            "loop-memory-wrong",
            written = mapOf(MEMORY to AgentMemoryCases.WRONG)
        )

        measured(
            vault,
            AgentMemoryCases.poisoning,
            "loop-memory-poisoned",
            written = AgentMemoryCases.POISONED
        )
    }

    // The one loop on agent mode's 30 tasks (step 6).
    @Test
    fun `the one loop is measured on the tasks of agent mode`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(
            vault = VAULT,
            all = AgentEvalCases.all,
            name = "loop-eval"
        )
    }

    // The one loop on the wider tasks (docs/agent-loop.md, step 5): documents, long notes, requests read two ways.
    @Test
    fun `the one loop is measured on documents, long notes and requests read two ways`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

            measured(
                vault = AgentWideCases.VAULT,
                all = AgentWideCases.all.map { it.forLoop() },
                name = "loop-wide"
            )
        }

    // docs/eval-fresh.md: a vault and tasks written blind, to tell whether the loop generalises.
    @Test
    fun `the one loop is measured on a fresh vault it was never tuned on`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        measured(
            vault = AgentFreshCases.VAULT,
            all = AgentFreshCases.all.map { it.forLoop() },
            name = "loop-fresh"
        )
    }

    // docs/eval-blind.md: nine more vaults written blind, measured before and after a change to the loop only.
    @Test
    fun `blind exams on nine vaults the loop was never tuned on`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        BlindExams.all.forEach { exam ->
            measured(
                vault = exam.vault,
                all = exam.cases.map { it.forLoop() },
                name = "blind-${exam.id}"
            )
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

        exams.forEach { exam ->
            measured(
                vault = exam.vault,
                all = exam.cases.map { it.forLoop() },
                name = "pool-${exam.id}"
            )
        }
    }

    /** [all] worked on [vault], and the report written to `build/reports/[name]-<size>.md`. */
    private suspend fun measured(
        vault: String,
        all: List<AgentEvalCase>,
        name: String,
        written: Map<String, String> = emptyMap()
    ) {
        val indexed = EvalVault.of(
            vault = vault,
            embedder = embedder,
            dispatchers = dispatchers
        )

        // Only the tasks a round is narrowed to, by words or by a list (`EvalTasks`).
        val picked = EvalTasks.fromProperties()
        val cases = all.filter { picked.holds(task = it.task) }

        if (cases.isEmpty()) return

        val notes = AgentEvalSetup.withWritten(
            indexed = indexed,
            notes = AgentEvalSetup.withMetadata(indexed, dispatchers),
            written = written
        )

        val cache = NoteTextCache(contentStore = indexed.store)
        val runner = AgentEvalSetup.runnerOf(indexed, cache, engine)

        val loop = LoopEval.loopOf(
            engine = recorded,
            embedder = embedder,
            indexed = indexed,
            runner = runner,
            trace = trace
        )

        val slots = Semaphore(permits = setup.slots)

        val results = coroutineScope {
            cases
                .map { case ->
                    async {
                        slots.withPermit {
                            val start = System.nanoTime()

                            val context = AgentEvalSetup.contextOf(notes = notes)

                            val steps = loop(case.task, indexed.path, notes, context, null).toList()

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
    }

    private companion object {
        const val VAULT = "Sample Vault"
        val MEMORY = VaultGuide.MEMORY.value
        const val GPU_SLOTS = 4
        const val NANOS_PER_SECOND = 1_000_000_000L
    }
}

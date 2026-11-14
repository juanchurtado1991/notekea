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
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okio.FileSystem
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Agent mode's evaluation (4.7): every task of [AgentEvalCases] worked on the committed Sample Vault with the real
 * models, its moves and end written to `build/reports/agent-eval-<size>.md`. A size is offered agent mode only when
 * it reaches [OFFER_BAR]. Run by hand like [AnswerEvalChatModelTest], with `--tests '*AgentEval*'`.
 */
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

    // The last prompt of each task's moves, by its task, for the report of a task that went wrong.
    private val lastPrompts = ConcurrentHashMap<String, String>()

    private val recorded = object : LlmEngine by engine {
        override fun generate(
            messages: List<ChatMessage>,
            options: GenerationOptions
        ): Flow<String> {
            val prompt = messages.last().text

            val task = TASK
                .find(input = prompt)
                ?.groupValues
                ?.get(1)

            if (task != null) lastPrompts[task] = prompt

            return engine.generate(
                messages = messages,
                options = options
            )
        }
    }

    @AfterTest
    fun tearDown() {
        engine.close()
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

    /** The share of [cases] done right on [vault], and the report written to `build/reports/[name]-<size>.md`. */
    private suspend fun measured(
        vault: String,
        all: List<AgentEvalCase>,
        name: String,
        isLoop: Boolean = false
    ): Pair<Double, String> {
        val indexed = EvalVault.of(
            vault = vault,
            embedder = embedder,
            dispatchers = dispatchers
        )

        // -PchatTasks="budget,reading challenge" runs only the tasks that hold one of those words.
        val picked = System
            .getProperty("notekea.chat.tasks")
            .orEmpty()
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        val cases = all.filter { case ->
            picked.isEmpty() || picked.any { it in case.task.lowercase() }
        }

        if (cases.isEmpty()) return 1.0 to ""

        val notes = withMetadata(indexed = indexed)
        val agent = agentOf(indexed = indexed)

        val runner = runnerOf(
            indexed = indexed,
            cache = NoteTextCache(indexed.store, TextFolder())
        )

        val loop = LoopEval.loopOf(recorded, embedder, indexed, runner)
        val slots = Semaphore(permits = setup.slots)

        val results = coroutineScope {
            cases
                .map { case ->
                    async {
                        slots.withPermit {
                            val start = System.nanoTime()

                            val context = contextOf(notes = notes)

                            val steps = if (isLoop) {
                                loop(case.task, indexed.path, notes, context, null).toList()
                            } else {
                                agent(case.task, indexed.path, notes, context, null).toList()
                            }

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

        val report = report(
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

    // Nothing is ever run in the evaluation: the runner's find is the vault's, the rest a fake.
    private fun runnerOf(
        indexed: EvalVault,
        cache: NoteTextCache
    ): ActionRunner {
        val apply = ApplyLinkUpdatesUseCase(noteContentStore = indexed.store)
        val calendar = JvmLocalCalendar()

        val finder = VaultActionFinder(
            TranslateQueryUseCase(engine, GhostQueryJsonReader()),
            BuildQueryCatalogUseCase(),
            RunQueryUseCase(),
            QueryYaml(),
            ListTasksUseCase(),
            SetTaskDoneUseCase(indexed.store, apply),
            SetPropertyUseCase(indexed.store, apply),
            cache,
            JetBrainsMarkdownLinkFinder(),
            calendar,
            Clock.System
        )

        return object : ActionRunner by FakeActionRunner() {
            override suspend fun find(
                vaultPath: String,
                step: ActionStep,
                notes: Collection<NoteEntry>
            ) = finder.find(
                vaultPath = vaultPath,
                step = step,
                notes = notes
            )
        }
    }

    private fun agentOf(indexed: EvalVault): AgentTaskUseCase {
        val cache = NoteTextCache(
            contentStore = indexed.store,
            textFolder = TextFolder()
        )

        val runner = runnerOf(
            indexed = indexed,
            cache = cache
        )

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

    /** The vault's notes with the properties, tags and tasks of their committed text, read from a copy on disk. */
    private suspend fun withMetadata(indexed: EvalVault): List<NoteEntry> {
        val copy = Files.createTempDirectory("notekea-agent").toFile()

        val reader =
            OkioNoteMetadataReader(FileSystem.SYSTEM, dispatchers, GhostFrontmatterParser())

        return indexed.notes.map { note ->
            val target = File(copy, note.file.path.value)
            target.parentFile.mkdirs()

            target.writeText(
                text = indexed
                    .store
                    .texts
                    .getValue(note.file.path)
            )

            note.copy(metadata = reader.read(copy.path, note.file.path) ?: note.metadata)
        }
    }

    private fun contextOf(notes: List<NoteEntry>) = ActionContext(
        today = "2026-11-11",
        folders = ActionContext.foldersOf(paths = notes.map { it.file.path }),
        notes = notes.map {
            it
                .file
                .path
                .value
                .removeSuffix(suffix = ".md")
        },
        properties = notes.flatMap { note ->
            note
                .metadata
                .properties
                .map { it.key }
        }.distinct(),
        tags = notes.flatMap { it.metadata.tags }.distinct()
    )

    private fun report(
        vault: String,
        results: List<AgentResult>,
        right: Int
    ): String = buildString {
        val moves = results.map {
            it.steps.count { step ->
                step is AgentStep.Moving
            }
        }

        append("# Agent mode on $vault (${size.id})\n\n")
        append("$right of ${results.size} right (${right * PERCENT / results.size} %), ")
        append("moves ${moves.average().toInt()} on average (${moves.max()} at most), ")
        append("${results.map { it.seconds }.average().toInt()} s a task on average (${results.maxOf { it.seconds }} s at most)\n")

        results.forEach { result ->
            append("\n## ${if (result.problem == null) "✓" else "✗"} ${result.case.task}\n\n")

            result.steps.forEach { append("- ${shown(step = it)}\n") }

            result.problem?.let {
                append(
                    failed(
                        result = result,
                        problem = it
                    )
                )
            }

            append("\n${result.seconds} s\n")
        }
    }

    private fun shown(step: AgentStep): String = when (step) {
        is AgentStep.Moving -> "${step.move} ${step.target}"
        else -> step.toString().take(n = STEP_CHARS)
    }

    // What went wrong, with what the agent had found by its last move.
    private fun failed(
        result: AgentResult,
        problem: String
    ): String = buildString {
        append("\n**$problem**\n")

        lastPrompts[result.case.task]?.let { prompt ->
            append("\n<details><summary>Found by the last move</summary>\n\n```\n")
            append(prompt.substringAfter(delimiter = "so far:\n").take(n = PROMPT_CHARS))
            append("\n```\n</details>\n")
        }
    }

    private class AgentResult(
        val case: AgentEvalCase,
        val steps: List<AgentStep>,
        val seconds: Long,
        val problem: String?
    )

    private companion object {
        const val VAULT = "Sample Vault"
        const val GPU_SLOTS = 4
        const val PERCENT = 100
        const val PROMPT_CHARS = 4_000
        const val STEP_CHARS = 600
        val TASK = Regex(pattern = "Task: (.+)")
        const val NANOS_PER_SECOND = 1_000_000_000L

        // A size is offered agent mode when four tasks in five come out right.
        const val OFFER_BAR = 0.8

        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}

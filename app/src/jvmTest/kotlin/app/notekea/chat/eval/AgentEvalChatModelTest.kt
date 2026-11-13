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
import app.notekea.shell.data.VaultActionFinder
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

            val indexed = EvalVault.of(
                vault = VAULT,
                embedder = embedder,
                dispatchers = dispatchers
            )

            val notes = withMetadata(indexed = indexed)
            val agent = agentOf(indexed = indexed)
            val slots = Semaphore(permits = setup.slots)

            val results = coroutineScope {
                AgentEvalCases
                    .all
                    .map { case ->
                        async {
                            slots.withPermit {
                                val start = System.nanoTime()

                                val steps = agent(
                                    task = case.task,
                                    vaultPath = indexed.path,
                                    notes = notes,
                                    context = contextOf(notes = notes),
                                    openNote = null
                                ).toList()

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

            val share = right.toDouble() / results.size

            val report = report(
                results = results,
                right = right
            )

            File("build/reports/agent-eval-${size.id}.md").run {
                parentFile.mkdirs()
                writeText(text = report)
            }

            println(
                report
                    .lines()
                    .take(n = 3)
                    .joinToString(separator = "\n")
            )

            assertTrue(
                actual = share >= OFFER_BAR,
                message = report
            )
        }

    private fun agentOf(indexed: EvalVault): AgentTaskUseCase {
        val cache = NoteTextCache(
            contentStore = indexed.store,
            textFolder = TextFolder()
        )

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

        // Agent mode only looks things up: the runner's find is the vault's, and nothing is ever run.
        val runner = object : ActionRunner by FakeActionRunner() {
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
        results: List<AgentResult>,
        right: Int
    ): String = buildString {
        val moves = results.map {
            it.steps.count { step ->
                step is AgentStep.Moving
            }
        }

        append("# Agent mode on the Sample Vault (${size.id})\n\n")
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
            append(prompt.substringAfter(delimiter = "Found so far:\n").take(n = PROMPT_CHARS))
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

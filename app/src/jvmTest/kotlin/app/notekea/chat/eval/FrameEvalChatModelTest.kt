package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.loop.TaskFramer
import app.notekea.chat.domain.loop.TurnFrames
import app.notekea.chat.domain.loop.TurnRouter
import app.notekea.chat.domain.loop.TurnSetup
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.eval.blind.BlindExams
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.search.eval.EvalVault
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
 * The frame alone, without the loop (`docs/loop-goal-plan.md`, step 1.2): for each task that has an end, whether the
 * frame is open when the task must be asked back and settled when it must not. Minutes, not the loop's hour.
 * Never on the fresh vault or the pool, whose tasks are read by cause only.
 */
class FrameEvalChatModelTest {
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
        slots = SLOTS,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = natives,
        dispatchers = dispatchers
    )

    @AfterTest
    fun tearDown() {
        engine.close()
        embedder.close()
    }

    @Test
    fun `the frame alone is measured on the tuned and development tasks`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val wide = AgentWideCases.VAULT
        val memory = VaultGuide.MEMORY.value
        val settling = mapOf(memory to AgentMemoryCases.SETTLING)

        val clear = AgentUnseenCases.all.filter {
            it.ends != AgentWideCase.End.ASK
        }

        val tuned = listOf(
            framed(wide, AgentWideCases.all),
            framed(wide, AgentWideHeldOutCases.all),
            framed(wide, AgentUnseenCases.all),
            framed(wide, AgentMemoryCases.settled + clear + AgentMemoryCases.uses, settling),
            framed(wide, AgentMemoryCases.followed, mapOf(memory to AgentMemoryCases.WRONG))
        ).flatten()

        val development = BlindExams.all.flatMap { framed(it.vault, it.cases) }

        write(
            name = "frame-tuned",
            verdicts = tuned
        )

        write(
            name = "frame-dev",
            verdicts = development
        )
    }

    // Each task's frame on [vault], built as a turn builds it: routed, its memory, then the frame.
    private suspend fun framed(
        vault: String,
        cases: List<AgentWideCase>,
        written: Map<String, String> = emptyMap()
    ): List<FrameVerdict> {
        val indexed = EvalVault.of(
            vault = vault,
            embedder = embedder,
            dispatchers = dispatchers
        )

        val notes = AgentEvalSetup.withWritten(
            indexed = indexed,
            notes = AgentEvalSetup.withMetadata(indexed, dispatchers),
            written = written
        )

        val cache = NoteTextCache(
            contentStore = indexed.store,
            textFolder = TextFolder
        )

        // A trace of each set's own, as the memory's sets ask the unseen tasks again.
        val trace = EvalTrace(engine = engine)
        val vaultGuide = VaultGuide(noteTextCache = cache)
        val router = TurnRouter(llmEngine = trace.recorded)

        val frames = TurnFrames(
            framer = TaskFramer(llmEngine = trace.recorded),
            answerFromNotes = AnswerFromNotesUseCase(embedder, indexed.index, cache, engine),
            vaultGuide = vaultGuide
        )

        val slots = Semaphore(permits = SLOTS)

        return coroutineScope {
            cases
                .map { case ->
                    async {
                        slots.withPermit {
                            val route = router.route(
                                task = case.task,
                                skills = vaultGuide.skillsOf(indexed.path, notes)
                            )

                            val remembered = vaultGuide.memoryOf(indexed.path, notes)
                            val relevant = TurnSetup.relevantOf(case.task, remembered, route.kind)

                            val frame = if (route.kind.isReply) {
                                null
                            } else {
                                TurnSetup.frameOf(
                                    task = case.task,
                                    vaultPath = indexed.path,
                                    notes = notes,
                                    memory = relevant,
                                    kind = route.kind,
                                    frames = frames,
                                    openNote = null
                                )
                            }

                            FrameVerdict(
                                task = case.task,
                                mustAsk = case.ends == AgentWideCase.End.ASK,
                                frame = frame,
                                calls = trace.of(task = case.task)
                            )
                        }
                    }
                }
                .awaitAll()
        }
    }

    private fun write(
        name: String,
        verdicts: List<FrameVerdict>
    ) {
        val report = FrameEvalReport.of(
            title = "The frame alone, $name (${size.id})",
            verdicts = verdicts
        )

        File("build/reports/$name-${size.id}.md").run {
            parentFile.mkdirs()
            writeText(text = report)
        }

        println(
            report
                .lines()
                .take(n = 4)
                .joinToString(separator = "\n")
        )
    }

    private companion object {
        const val SLOTS = 4

        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}

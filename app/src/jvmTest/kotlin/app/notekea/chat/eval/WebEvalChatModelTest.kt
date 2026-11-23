package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.clips.data.DuckDuckGoSearch
import app.notekea.clips.data.KtorPageFetcher
import app.notekea.clips.data.ReadabilityArticleExtractor
import app.notekea.clips.domain.ClipSectionUseCase
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.DescribeSectionUseCase
import app.notekea.clips.domain.FindSectionUseCase
import app.notekea.clips.domain.SectionRanker
import app.notekea.clips.domain.SupportCheck
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.web.domain.SourceTrust
import app.notekea.search.eval.EvalVault
import app.notekea.shell.data.chat.ClipsForChat
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
import kotlin.time.Clock

/**
 * The web's evaluation (`docs/agent-web.md`, "How it is measured"): the 50 tasks of [WebEvalCases] worked by the one
 * loop on the real web, searched with DuckDuckGo and kept in `-PwebPages` so every model reads the same pages. The
 * person is played by the run: the sources it would tick are read. Graded by a person, from the report.
 */
class WebEvalChatModelTest {
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

    private val trace = EvalTrace(engine = engine)

    private val pages = RecordedPageFetcher(
        real = KtorPageFetcher(dispatchers = dispatchers),
        folder = File(
            System
                .getProperty("notekea.web.pages")
                .orEmpty()
                .ifEmpty { "build/web-pages" }
        ),
        isReplayOnly = System.getProperty("notekea.web.replay") == "true"
    )

    private val extractor = ReadabilityArticleExtractor()
    private val search = DuckDuckGoSearch(fetcher = pages)

    // The app's own reader and clipper, so a page clipped in a proposal is clipped as the chat would.
    private val clips = ClipsForChat(
        clipWebPage = ClipWebPageUseCase(
            fetcher = pages,
            extractor = extractor,
            llmEngine = engine,
            supportCheck = SupportCheck(llmEngine = engine)
        ),
        findSection = FindSectionUseCase(
            fetcher = pages,
            ranker = SectionRanker(textEmbedder = embedder)
        ),
        clipSection = ClipSectionUseCase(
            clipWebPage = ClipWebPageUseCase(
                fetcher = pages,
                extractor = extractor,
                llmEngine = engine,
                supportCheck = SupportCheck(llmEngine = engine)
            )
        ),
        describeSection = DescribeSectionUseCase(llmEngine = engine),
        fetcher = pages,
        extractor = extractor,
        localCalendar = JvmLocalCalendar(),
        clock = Clock.System
    )

    @AfterTest
    fun tearDown() {
        engine.close()
        embedder.close()
    }

    @Test
    fun `the one loop is measured on the web's tasks`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val picked = System
            .getProperty("notekea.chat.tasks")
            .orEmpty()
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        val cases = WebEvalCases.all.filter { case ->
            picked.isEmpty() || picked.any { it in case.task.lowercase() }
        }

        val slots = Semaphore(permits = SLOTS)

        val results = coroutineScope {
            cases
                .map { case ->
                    async {
                        slots.withPermit { worked(case = case) }
                    }
                }
                .awaitAll()
        }

        File("build/reports/web-eval-${size.id}.md").run {
            parentFile.mkdirs()

            writeText(
                text = WebEvalReport.of(
                    size = size.id,
                    results = results,
                    trace = trace
                )
            )
        }

        Unit
    }

    // The task worked as the person would go on with it, a turn at a time.
    private suspend fun worked(case: WebEvalCase): WebEvalResult {
        val start = System.nanoTime()

        val indexed = EvalVault.of(
            vault = AgentWideCases.VAULT,
            embedder = embedder,
            dispatchers = dispatchers
        )

        val notes = AgentEvalSetup.withWritten(
            indexed = indexed,
            notes = AgentEvalSetup.withMetadata(indexed, dispatchers),
            written = case
                .memory
                ?.let { mapOf(VaultGuide.MEMORY.value to it) }
                .orEmpty()
        )

        val cache = NoteTextCache(indexed.store, TextFolder)
        val runner = AgentEvalSetup.runnerOf(indexed, cache, engine)

        val loop = LoopEval.loopOf(trace.recorded, embedder, indexed, runner, clips, search, clips)
        val context = AgentEvalSetup.contextOf(notes = notes)

        // The person's turns: the task, then the web button when the notes do not hold it, then the sources ticked.
        val turns = mutableListOf<WebEvalResult.Turn>()
        var asked: String? = case.task

        while (asked != null && turns.size < MOST_TURNS) {
            val steps = loop(asked, indexed.path, notes, context, null).toList()

            turns += WebEvalResult.Turn(
                asked = asked,
                steps = steps
            )

            asked = nextOf(
                task = case.task,
                asked = asked,
                end = steps.last()
            )
        }

        return WebEvalResult(
            case = case,
            turns = turns,
            seconds = (System.nanoTime() - start) / NANOS_PER_SECOND
        )
    }

    // What the person does next: clicks Search the web on a not found, or reads the sources they would tick.
    private fun nextOf(
        task: String,
        asked: String,
        end: AgentStep
    ): String? = when {
        end is AgentStep.Finished &&
            AnswerPrompt.isNotFound(text = end.summary) &&
            asked == task ->
            if (SPANISH.containsMatchIn(input = task)) "Busca en internet: $task" else "Search the web: $task"

        end is AgentStep.SourcesFound -> {
            val trusted = end.sources.filter {
                it.trust == SourceTrust.Trust.TRUSTED
            }

            val new = end
                .sources
                .filter {
                    it.trust == SourceTrust.Trust.NEW
                }
                .take(n = NEW_TICKED)

            "$asked\n\n${(trusted + new).joinToString(separator = "\n") { it.result.url }}"
        }

        else -> null
    }

    private companion object {
        // Two tasks at a time: the searches wait for each other anyway, and the sites are asked politely.
        const val SLOTS = 2
        const val NEW_TICKED = 3
        const val MOST_TURNS = 3
        val SPANISH =
            Regex(pattern = "[¿¡ñáéíóú]|\\b(dónde|qué|cuál|cuánto|busca|investiga|haz|guarda)\\b")
        const val NANOS_PER_SECOND = 1_000_000_000L

        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}

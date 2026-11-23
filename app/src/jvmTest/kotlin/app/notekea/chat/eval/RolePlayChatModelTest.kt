package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.agent.AgentStep
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
import app.notekea.search.eval.EvalVault
import app.notekea.shell.data.chat.ClipsForChat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * A person played by hand: the chat's loop on `Agent Eval`, with the memory and the web, takes one message at a time
 * from `in-<n>.txt` in `-ProlePlay` and writes its turn to `out-<n>.md`, the conversation kept as the chat keeps it.
 * `STOP` ends it. Changes are proposed and never made, as in the evaluations.
 */
class RolePlayChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.LARGE
    private val talk = File(System.getProperty("notekea.roleplay").orEmpty())
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
        slots = 1,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = natives,
        dispatchers = dispatchers
    )

    private val pages = RecordedPageFetcher(
        real = KtorPageFetcher(dispatchers = dispatchers),
        folder = File(
            System
                .getProperty("notekea.web.pages")
                .orEmpty()
                .ifEmpty { "build/web-pages" }
        )
    )

    private val extractor = ReadabilityArticleExtractor()

    private val clip = ClipWebPageUseCase(
        fetcher = pages,
        extractor = extractor,
        llmEngine = engine,
        supportCheck = SupportCheck(llmEngine = engine)
    )

    private val clips = ClipsForChat(
        clipWebPage = clip,
        findSection = FindSectionUseCase(
            fetcher = pages,
            ranker = SectionRanker(textEmbedder = embedder)
        ),
        clipSection = ClipSectionUseCase(clipWebPage = clip),
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
    fun `a person talks with the chat one message at a time`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)) && talk.isDirectory)

        val indexed = EvalVault.of(
            vault = AgentWideCases.VAULT,
            embedder = embedder,
            dispatchers = dispatchers
        )

        val notes = AgentEvalSetup.withWritten(
            indexed = indexed,
            notes = AgentEvalSetup.withMetadata(indexed, dispatchers),
            written = mapOf(VaultGuide.MEMORY.value to WebEvalCases.COSTA_RICA)
        )

        val cache = NoteTextCache(indexed.store, TextFolder)
        val runner = AgentEvalSetup.runnerOf(indexed, cache, engine)
        val search = DuckDuckGoSearch(fetcher = pages)
        val loop = LoopEval.loopOf(engine, embedder, indexed, runner, clips, search, clips)
        val history = mutableListOf<ChatExchange>()

        var number = 1

        while (true) {
            val asked = messageOf(number = number) ?: break
            if (asked.trim() == STOP) break

            val context = AgentEvalSetup
                .contextOf(notes = notes)
                .copy(history = history.toList())

            val steps = loop(asked, indexed.path, notes, context, null).toList()

            File(talk, "out-$number.md").writeText(text = steps.joinToString(separator = "\n") { "- $it" })

            history += ChatExchange(
                question = asked,
                reply = replyOf(end = steps.last())
            )

            number++
        }
    }

    // The person's next message, waited for until it is written; null when none comes in the time allowed.
    private suspend fun messageOf(number: Int): String? {
        val file = File(talk, "in-$number.txt")

        repeat(times = WAITS) {
            if (file.isFile) return file.readText()
            delay(duration = WAIT)
        }

        return null
    }

    // What the conversation keeps of a turn, as the chat's history does.
    private fun replyOf(end: AgentStep): String = when (end) {
        is AgentStep.Finished -> end.summary
        is AgentStep.Asked -> "${end.question} ${end.options.joinToString(separator = " / ")}"
        is AgentStep.Proposed -> end.summary
        is AgentStep.SourcesFound -> "Found sources: ${end.sources.joinToString { it.result.url }}"
        else -> end.toString()
    }

    private companion object {
        const val STOP = "STOP"
        const val WAITS = 720
        val WAIT = 5.seconds

        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}

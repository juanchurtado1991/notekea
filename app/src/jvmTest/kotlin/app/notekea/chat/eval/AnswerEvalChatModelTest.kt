package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.domain.AnswerStep
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.search.eval.EvalVault
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The answer evaluation (3.4): every question of `smoke-data/eval/answers.json` through the app's answer, on the real
 * index, embedding model, chat model and reranker, graded strictly. Run by hand for one size:
 * `./gradlew :app:chatModelTest -PchatModels=<folder> -PchatSize=small --tests '*AnswerEval*'`.
 */
class AnswerEvalChatModelTest {
    private val folder = Path.of(System.getProperty(MODELS_PROPERTY).orEmpty())
    private val size = ModelSize.fromId(id = System.getProperty(SIZE_PROPERTY)) ?: ModelSize.SMALL
    private val onGpu = System.getProperty(GPU_PROPERTY) != "false"
    private val dispatchers = JvmAppDispatchers()
    private val natives = Files.createTempDirectory("notekea-natives").toString()
    private val embedder = LlamaTextEmbedder(
        modelPath = System.getProperty(EMBEDDING_PROPERTY).orEmpty(),
        nativeFolder = natives
    )
    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve(RERANKER_FILE).toString(),
        slots = if (onGpu) GPU_SLOTS else 1,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = onGpu
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
    fun `the answers to the evaluation set are right at or above the baseline`() = runBlocking {
        val isReady = listOf(setup.modelPath, setup.rerankerPath).all {
            Files.isRegularFile(Path.of(it))
        }

        assumeTrue(isReady)

        val answered = AnswerEvalSet
            .read()
            .groupBy { it.vault }
            .flatMap { (vault, asked) ->
                answerAll(
                    vault = vault,
                    asked = asked
                )
            }

        val label = "${size.id} on the ${if (onGpu) "GPU" else "CPU"}"

        val report = AnswerEvalReport(
            label = label,
            answered = answered
        )

        File(REPORTS, "answer-eval-${size.id}.txt").run {
            parentFile.mkdirs()
            writeText(text = report.text() + "\n")
        }

        File(REPORTS, "answer-eval-${size.id}.tsv").writeText(
            text = answered
                .joinToString(separator = "\n") {
                    val answer = it.answer ?: NOT_FOUND

                    listOf(it.isRight, it.question.question, answer).joinToString(separator = "\t")
                } + "\n"
        )

        println(report.text())

        assertTrue(
            actual = report.right >= BASELINES.getValue(size),
            message = report.text()
        )
    }

    private suspend fun answerAll(
        vault: String,
        asked: List<AnswerQuestion>
    ): List<AnsweredQuestion> {
        val indexed = EvalVault.of(
            vault = vault,
            embedder = embedder,
            dispatchers = dispatchers
        )

        val answer = AnswerFromNotesUseCase(
            textEmbedder = embedder,
            vectorIndex = indexed.index,
            noteTextCache = NoteTextCache(
                contentStore = indexed.store,
                textFolder = TextFolder()
            ),
            llmEngine = engine
        )

        val slots = Semaphore(permits = setup.slots)

        return kotlinx.coroutines.coroutineScope {
            asked.map { question ->
                async {
                    slots.withPermit {
                        answerOne(
                            answer = answer,
                            indexed = indexed,
                            question = question
                        )
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun answerOne(
        answer: AnswerFromNotesUseCase,
        indexed: EvalVault,
        question: AnswerQuestion
    ): AnsweredQuestion {
        val start = System.nanoTime()
        var first: Long? = null
        var last: AnswerStep = AnswerStep.NotFound

        answer(
            vaultPath = indexed.path,
            notes = indexed.notes,
            question = question.question
        ).collect { step ->
            if (step is AnswerStep.Writing && first == null) first = millisSince(start = start)
            last = step
        }

        val done = (last as? AnswerStep.Done)?.answer

        return AnsweredQuestion(
            question = question,
            read = done?.read.orEmpty(),
            cited = done?.sources.orEmpty(),
            totalMillis = millisSince(start = start),
            answer = done?.text,
            firstMillis = first,
            failure = (last as? AnswerStep.Failed)?.error?.toString()
        )
    }

    private fun millisSince(start: Long): Long = (System.nanoTime() - start) / NANOS_PER_MILLI

    private companion object {
        const val MODELS_PROPERTY = "notekea.chat.models"
        const val SIZE_PROPERTY = "notekea.chat.size"
        const val GPU_PROPERTY = "notekea.chat.gpu"
        const val EMBEDDING_PROPERTY = "notekea.embedding.model"
        const val RERANKER_FILE = "bge-reranker-v2-m3-Q4_K_M.gguf"
        const val REPORTS = "build/reports"
        const val NOT_FOUND = "NOT_FOUND"
        const val GPU_SLOTS = 4
        const val NANOS_PER_MILLI = 1_000_000L
        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )

        // Three points under the first run of 3.4, 80 / 89 / 85 % (docs/answer-quality.md): batching on the GPU moves
        // an answer now and then.
        val BASELINES = mapOf(
            ModelSize.SMALL to 0.77,
            ModelSize.MEDIUM to 0.86,
            ModelSize.LARGE to 0.82
        )
    }
}

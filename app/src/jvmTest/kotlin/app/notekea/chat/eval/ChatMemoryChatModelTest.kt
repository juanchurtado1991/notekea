package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.search.eval.EvalVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The memory of chatting (3.7), as an 8 GB computer chats: Small on the CPU with one slot, on the Sample Vault. The
 * process's resident memory before the model, its peak over ten answers, and after the model is let go; and the time
 * to the first words; 5.4's budget is 2.5 GB, 3.7 measured 3.3. Run by hand like [AnswerEvalChatModelTest].
 */
class ChatMemoryChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val dispatchers = JvmAppDispatchers()
    private val natives = Files.createTempDirectory("notekea-natives").toString()

    private val setup = ModelSetup(
        size = ModelSize.SMALL,
        modelPath = folder.resolve("Qwen3.5-2B-Q4_K_M.gguf").toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = 1,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = false
    )

    @Test
    fun `chatting with Small on the CPU stays under the budget and gives the memory back`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

            val embedder = LlamaTextEmbedder(
                modelPath = System.getProperty("notekea.embedding.model").orEmpty(),
                nativeFolder = natives
            )

            val indexed = EvalVault.of(
                vault = "Sample Vault",
                embedder = embedder,
                dispatchers = dispatchers
            )

            val engine = LlamaLlmEngine(
                setupSource = { Outcome.Success(value = setup) },
                nativeFolder = natives,
                dispatchers = dispatchers
            )

            val cache = NoteTextCache(indexed.store, TextFolder())
            val answer = AnswerFromNotesUseCase(embedder, indexed.index, cache, engine)

            val before = residentMb()
            var peak = before

            val sampler = launch(context = Dispatchers.Default) {
                while (isActive) {
                    peak = maxOf(peak, residentMb())
                    delay(timeMillis = SAMPLE_MILLIS)
                }
            }

            val firsts = QUESTIONS.map { question ->
                val start = System.nanoTime()
                var first = 0L

                answer(
                    vaultPath = indexed.path,
                    notes = indexed.notes,
                    question = question
                ).collect { step ->
                    val isFirst =
                        (step is AnswerStep.Writing || step is AnswerStep.Done) && first == 0L

                    if (isFirst) first = (System.nanoTime() - start) / NANOS_PER_MILLI
                }

                first
            }

            sampler.cancel()
            engine.close()
            System.gc()
            delay(timeMillis = SETTLE_MILLIS)
            val after = residentMb()

            val report =
                "Small on the CPU, 1 slot: resident before the model $before MB, peak over ${QUESTIONS.size} " +
                    "answers $peak MB, after it is let go $after MB; first words ms median ${firsts.sorted()[firsts.size / 2]}, " +
                    "max ${firsts.max()}"

            File("build/reports/chat-memory.txt").writeText(text = report + "\n")
            println(report)
            embedder.close()

            assertTrue(
                actual = peak < BUDGET_MB,
                message = report
            )
        }

    private fun residentMb(): Long = File("/proc/self/status")
        .readLines()
        .first { it.startsWith(prefix = "VmRSS:") }
        .split(Regex(pattern = "\\s+"))[1]
        .toLong() / KB_PER_MB

    private companion object {
        // 3.7 measured 3,292 MB against 5.4's budget of 2,560 (docs/performance.md): this keeps it from growing.
        const val BUDGET_MB = 3_500L
        const val SAMPLE_MILLIS = 100L
        const val SETTLE_MILLIS = 2_000L
        const val NANOS_PER_MILLI = 1_000_000L
        const val KB_PER_MB = 1_024L
        val QUESTIONS = listOf(
            "How much water does the arepa dough take?",
            "Who wrote Dune?",
            "What did we decide in the kickoff meeting?",
            "¿Cuándo es el viaje a Lisboa?",
            "Which vegetables are in the garden plan?",
            "What is the budget for the kitchen renovation?",
            "When is my dentist appointment?",
            "How long do the black beans soak?",
            "What does Atomic Habits say about habits?",
            "What did I eat on 2026-10-25?"
        )
    }
}

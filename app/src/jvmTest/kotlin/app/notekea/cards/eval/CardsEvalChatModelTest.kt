package app.notekea.cards.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.cards.data.GhostCardsReader
import app.notekea.cards.domain.MakeCardsUseCase
import app.notekea.chat.eval.EvalModels
import app.notekea.chat.eval.ReplyCache
import app.notekea.chat.eval.blind.BlindExams
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.RelativePath
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
 * The study cards measured (`docs/gemma-plan.md`, section 8, step 1): cards made from every note of the nine dev
 * vaults long enough to study, and from the long notes of the other vaults, read by counts (`CardsEvalReport`).
 * Run as `measure.sh <label> cards <size>`.
 */
class CardsEvalChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.MEDIUM
    private val dispatchers = JvmAppDispatchers()
    private val natives = Files.createTempDirectory("notekea-natives").toString()

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(EvalModels.FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = SLOTS,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val llama = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = natives,
        dispatchers = dispatchers
    )

    private val engine = ReplyCache.around(
        engine = llama,
        modelPath = setup.modelPath
    )

    @AfterTest
    fun tearDown() {
        (engine as? ReplyCache)?.note()
        llama.close()
    }

    @Test
    fun `the cards are measured on the dev vaults and the long notes`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val dev = BlindExams.all.flatMap { exam ->
            made(
                vault = exam.vault,
                texts = EvalVault
                    .committed(vault = exam.vault)
                    .filterValues {
                        it.length >= MIN_CHARS
                    }
            )
        }

        val long = LONG.flatMap { (vault, notes) ->
            made(
                vault = vault,
                texts = EvalVault
                    .committed(vault = vault)
                    .filterKeys {
                        it.value in notes
                    }
            )
        }

        write(
            name = "cards-dev",
            notes = dev
        )

        write(
            name = "cards-long",
            notes = long
        )
    }

    // The cards made from each of [texts], as the app makes them.
    private suspend fun made(
        vault: String,
        texts: Map<RelativePath, String>
    ): List<NoteCards> {
        val store = FakeNoteContentStore()
        store.texts.putAll(texts)

        val cards = MakeCardsUseCase(
            llmEngine = engine,
            reader = GhostCardsReader(),
            noteContentStore = store
        )

        val slots = Semaphore(permits = SLOTS)

        return coroutineScope {
            texts
                .entries
                .sortedBy { it.key.value }
                .map { (path, text) ->
                    async {
                        slots.withPermit {
                            val judged = cards.judged(
                                vaultPath = vault,
                                note = path
                            )

                            NoteCards(
                                vault = vault,
                                note = path.bare(),
                                text = text,
                                judged = (judged as? Outcome.Success)?.value.orEmpty(),
                                problem = (judged as? Outcome.Failure)?.error?.name
                            )
                        }
                    }
                }
                .awaitAll()
        }
    }

    private fun write(
        name: String,
        notes: List<NoteCards>
    ) {
        val report = CardsEvalReport(
            notes = notes,
            cut = CUT
        ).of(title = "The study cards, $name (${size.id})")

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
        const val SLOTS = 4

        // A note shorter than this is a list of links or a stub, with little to study.
        const val MIN_CHARS = 400

        // The part of a note the model is given (`CardsPrompt`).
        const val CUT = 8_000

        // The committed notes longer than the part the model is given, outside the blind vaults.
        val LONG = mapOf(
            "Agent Eval" to setOf(
                "Contracts/Service agreement.md",
                "Contracts/Contrato de arrendamiento.md",
                "Study/Sleep and memory, reading notes.md",
                "Documents/Volunteer handbook.md"
            ),
            "Ghost Serializer" to setOf(
                "ghost-serialization/ghost-serialization.md",
                "ghost-compiler/ghost-compiler.md"
            )
        )
    }
}

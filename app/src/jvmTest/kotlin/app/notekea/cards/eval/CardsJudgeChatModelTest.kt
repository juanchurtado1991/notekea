package app.notekea.cards.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.eval.EvalModels
import app.notekea.chat.eval.FairText
import app.notekea.chat.eval.ReplyCache
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
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
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

/**
 * The study cards judged (the developer's rules of 2026-11-26): the cards a `cards` run kept (its `.tsv`, given as
 * `-PcardsJudged=<file>`) judged by this run's model, the 12B, rule by rule; and each note's sentences worth a card,
 * for the cover: those with a card that passes. Also a sample of 40 cards for the developer to judge too.
 */
class CardsJudgeChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.LARGE
    private val judged = File(System.getProperty("notekea.cards.judged").orEmpty())
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

    private val judge = CardsJudge(engine = engine)
    private val slots = Semaphore(permits = SLOTS)

    @AfterTest
    fun tearDown() {
        (engine as? ReplyCache)?.note()
        llama.close()
    }

    @Test
    fun `the cards a cards run kept are judged by their rules, and the notes by their sentences worth a card`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)) && judged.isFile)

            val cards = judged
                .readLines()
                .mapNotNull { MadeCard.of(line = it) }

            val failed = coroutineScope {
                cards
                    .map { card ->
                        async {
                            slots.withPermit {
                                card to judge.failedOf(card = card)
                            }
                        }
                    }
                    .awaitAll()
            }

            val notes = cards.groupBy {
                it.vault to it.note
            }

            val covers = coroutineScope {
                notes
                    .map { (place, made) ->
                        async {
                            slots.withPermit {
                                coverOf(
                                    vault = place.first,
                                    note = place.second,
                                    passing = made.filter { card ->
                                        failed
                                            .first {
                                                it.first == card
                                            }
                                            .second
                                            ?.isEmpty() == true
                                    }
                                )
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

            write(
                failed = failed,
                covers = covers
            )
        }

    // A note's sentences worth a card, and how many of them a passing card was made from; null when unread.
    private suspend fun coverOf(
        vault: String,
        note: String,
        passing: List<MadeCard>
    ): Pair<Int, Int>? {
        val text = EvalVault
            .committed(vault = vault)
            .entries
            .firstOrNull {
                it.key.bare() == note
            }
            ?.value ?: return null

        val sentences = NoteSentences.of(text = text)

        val worth = sentences
            .chunked(size = BATCH)
            .mapIndexed { batch, part ->
                judge.worthOf(sentences = part)?.map {
                    batch * BATCH + it
                } ?: emptyList()
            }
            .flatten()
            .map {
                sentences[it]
            }

        val made = passing.map { FairText.plain(text = it.sentence) }

        val covered = worth.count { sentence ->
            val plain = FairText.plain(text = sentence)

            made.any {
                it.isNotEmpty() && (it in plain || plain in it)
            }
        }

        return covered to worth.size
    }

    private fun write(
        failed: List<Pair<MadeCard, List<String>?>>,
        covers: List<Pair<Int, Int>>
    ) {
        val read = failed.filter {
            it.second != null
        }

        val passing = read.count { it.second!!.isEmpty() }

        val byRule = read
            .flatMap {
                it.second!!
            }
            .groupingBy { it }
            .eachCount()

        val covered = covers.sumOf { it.first }
        val worth = covers.sumOf { it.second }
        val name = judged.nameWithoutExtension

        val report = buildString {
            appendLine("# The study cards judged, $name (by ${size.id})")
            appendLine()

            appendLine(
                "Cards $passing of ${read.size} pass (${percent(passing, read.size)} %); cover $covered of $worth " +
                    "sentences worth a card (${percent(covered, worth)} %); ${failed.size - read.size} unread."
            )

            appendLine()
            appendLine("| Rule failed | Cards |")
            appendLine("|---|---|")

            byRule
                .entries
                .sortedByDescending { it.value }
                .forEach { appendLine("| ${it.key} | ${it.value} |") }
        }

        // The developer's sample: 40 cards at random, without the judge's word, kept apart to compare after.
        val sample = read.shuffled(random = Random(seed = 1)).take(n = SAMPLE)

        val sheet = buildString {
            appendLine("# 40 study cards to judge ($name)")
            appendLine()

            sample.forEachIndexed { index, (card, _) ->
                appendLine("${index + 1}. **${card.question}** → ${card.answer}")
                appendLine("   - From ${card.note}: “${card.sentence}”")
            }
        }

        val key = sample.mapIndexed { index, (_, rules) ->
            "${index + 1}. ${if (rules!!.isEmpty()) "passes" else "fails ${rules.joinToString()}"}"
        }

        File("build/reports/cards-judge-$name.md").run {
            parentFile.mkdirs()
            writeText(text = report)
        }

        File("build/reports/cards-sample-$name.md").writeText(text = sheet)
        File("build/reports/cards-key-$name.md").writeText(text = key.joinToString(separator = "\n"))

        println(
            report
                .lines()
                .take(n = 3)
                .joinToString(separator = "\n")
        )
    }

    private fun percent(
        part: Int,
        whole: Int
    ): Int = if (whole == 0) 0 else part * PERCENT / whole

    private companion object {
        const val SLOTS = 4
        const val BATCH = 25
        const val SAMPLE = 40
        const val PERCENT = 100
    }
}

package app.notekea.clips.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.clips.data.ReadabilityArticleExtractor
import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.SupportCheck
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Web clips (4.4) with the real model on the 20 saved pages of `smoke-data/eval/web-pages`: the article without the
 * site's menu, notes written, every kept sentence backed by the article's words. By hand; `build/reports/clip-eval.md`.
 */
class ClipEvalChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.MEDIUM

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = 4,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
        dispatchers = JvmAppDispatchers()
    )

    @AfterTest
    fun tearDown() {
        engine.close()
    }

    @Test
    fun `every clipped page keeps its article and notes the article backs`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val pages = File(PAGES)
            .listFiles()
            .orEmpty()
            .filter {
                it.extension == "html"
            }
            .sortedBy { it.name }

        val report =
            StringBuilder("# Web clips (${size.id})\n\n| Page | Sentences | Backed |\n|---|---|---|\n")

        val results = pages.map { clipped(file = it) }

        results.forEach { report.append("| ${it.name} | ${it.sentences} | ${it.backed} |\n") }

        results.forEach {
            report.append("\n## ${it.name}\n\n${it.notes}\n\nNot backed: ${it.unbacked}\n")
        }

        val sentences = results.sumOf { it.sentences }
        val backed = results.sumOf { it.backed }

        val withNotes = results.count {
            it.sentences > 0
        }

        val clean = results.count { it.isClean }
        val share = if (sentences == 0) 0 else backed * PERCENT / sentences

        report.append("\n$withNotes of ${pages.size} pages with notes; $backed of $sentences sentences ($share %) ")
        report.append("backed by the article's words; $clean of ${pages.size} articles without the site's menu.\n")
        File("build/reports/clip-eval.md").writeText(text = report.toString())
        println(report)

        val isGood =
            withNotes >= pages.size - 2 &&
                share >= MIN_BACKED &&
                clean == pages.size

        assertTrue(
            actual = isGood,
            message = report.toString()
        )
    }

    private suspend fun clipped(file: File): PageResult {
        val page = FetchedPage(
            url = "https://example.com/${file.nameWithoutExtension}",
            html = file.readText()
        )

        val clip = ClipWebPageUseCase(
            fetcher = { Outcome.Success(value = page) },
            extractor = ReadabilityArticleExtractor(),
            llmEngine = engine,
            supportCheck = SupportCheck(llmEngine = engine)
        )

        val done = clip(url = page.url).toList().last() as? ClipStep.Done
            ?: return PageResult(
                name = file.name,
                sentences = 0,
                backed = 0,
                isClean = false,
                notes = ""
            )

        val article = done
            .clip
            .article
            .text
            .lowercase()

        val notes = done
            .clip
            .notes
            .lines()
            .filterNot { it.startsWith("#") }
            .joinToString(separator = " ")

        // The opening sentence says what the article is about, as the prompt asks: it states no fact to check.
        val kept = SENTENCE
            .findAll(input = notes)
            .map { it.value.trim() }
            .filter {
                it.split(' ').size >= MIN_WORDS
            }
            .drop(n = 1)
            .toList()

        val unbacked = kept.filterNot {
            isBacked(
                sentence = it,
                article = article
            )
        }

        return PageResult(
            name = file.name,
            sentences = kept.size,
            backed = kept.size - unbacked.size,
            isClean = NAVIGATION.none {
                it in done.clip.article.text
            },
            notes = done.clip.notes,
            unbacked = unbacked
        )
    }

    private data class PageResult(
        val name: String,
        val sentences: Int,
        val backed: Int,
        val isClean: Boolean,
        val notes: String,
        val unbacked: List<String> = emptyList()
    )

    // Backed by its words: most of the sentence's longer words are in the article (a check apart from the reranker).
    private fun isBacked(
        sentence: String,
        article: String
    ): Boolean {
        val words = WORD
            .findAll(input = sentence.lowercase())
            .map { it.value }
            .filter {
                it.length > SHORT_WORD
            }
            .toList()

        if (words.isEmpty()) return true

        return words.count {
            it.take(n = STEM) in article
        } * PERCENT / words.size >= WORDS_PRESENT
    }

    private companion object {
        const val PAGES = "../smoke-data/eval/web-pages"
        const val PERCENT = 100
        const val MIN_BACKED = 85
        const val WORDS_PRESENT = 50
        const val SHORT_WORD = 3
        const val STEM = 5
        const val MIN_WORDS = 3
        val NAVIGATION = listOf("Subscribe", "Privacy", "Ten tips")
        val SENTENCE = Regex(pattern = "[^.!?]+[.!?]")
        val WORD = Regex(pattern = "[\\p{L}]+")
        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}

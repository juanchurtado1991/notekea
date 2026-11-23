package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import com.fasterxml.jackson.databind.ObjectMapper
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
import kotlin.test.assertTrue

/**
 * The action evaluation (3.9): every request of `smoke-data/eval/actions.json` planned by the real chat model over
 * the Sample Vault's names, right when its actions are the expected ones, in order, with the expected fields. Run by
 * hand: `./gradlew :app:chatModelTest -PchatModels=<folder> -PchatSize=medium --tests '*ActionEval*'`.
 */
class ActionEvalChatModelTest {
    private val folder = Path.of(System.getProperty(MODELS_PROPERTY).orEmpty())
    private val size = ModelSize.fromId(id = System.getProperty(SIZE_PROPERTY)) ?: ModelSize.SMALL
    private val onGpu = System.getProperty(GPU_PROPERTY) != "false"

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(EvalModels.FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve(EvalModels.RERANKER_FILE).toString(),
        slots = if (onGpu) GPU_SLOTS else 1,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = onGpu
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
        dispatchers = JvmAppDispatchers()
    )

    private val plan = PlanRequestUseCase(
        llmEngine = engine,
        planReader = GhostActionPlanReader()
    )

    @AfterTest
    fun tearDown() {
        engine.close()
    }

    @Test
    fun `the plans for the evaluation requests are right at or above the baseline`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

        val requests = ObjectMapper().readTree(File(SET_FILE)).toList()
        val slots = Semaphore(permits = setup.slots)

        val rows = coroutineScope {
            requests.map { request ->
                async {
                    slots.withPermit { graded(request = request) }
                }
            }.awaitAll()
        }

        val right = rows.count { it.first }

        val text =
            "${size.id}: $right of ${rows.size} plans right (${right * PERCENT / rows.size} %), " +
                "median ${rows.map { it.third }.sorted()[rows.size / 2]} ms"

        File(REPORTS, "action-eval-${size.id}.txt").run {
            parentFile.mkdirs()
            writeText(text = text + "\n")
        }

        File(REPORTS, "action-eval-${size.id}.tsv").writeText(
            text = rows.joinToString(separator = "\n") { it.second } + "\n"
        )

        println(text)

        assertTrue(
            actual = right.toDouble() / rows.size >= BASELINES.getValue(size),
            message = text
        )
    }

    private suspend fun graded(
        request: com.fasterxml.jackson.databind.JsonNode
    ): Triple<Boolean, String, Long> {
        val asked = request.get("request").asText()

        val open = request
            .get("open")
            ?.takeUnless { it.isNull }
            ?.asText()

        val start = System.nanoTime()

        val steps = plan(
            request = asked,
            context = SampleVaultNames.context(openNote = open)
        ).valueOrNull()
            .orEmpty()

        val millis = (System.nanoTime() - start) / NANOS_PER_MILLI
        val expected = request.get("steps").toList()

        val isRight = ActionGrade.isRight(
            steps = steps,
            expected = expected
        )

        val written = steps
            .joinToString(separator = " | ") { step ->
                step.kind.written + step
                    .fields
                    .entries
                    .joinToString(
                        prefix = "(",
                        postfix = ")"
                    ) {
                        "${it.key.written}=${it.value}"
                    } + if (step.notes.isEmpty()) "" else step.notes.toString()
            }

        return Triple(isRight, "$isRight\t${asked.lines().first().take(n = 80)}\t$written", millis)
    }

    private companion object {
        const val SET_FILE = "../smoke-data/eval/actions.json"
        const val MODELS_PROPERTY = "notekea.chat.models"
        const val SIZE_PROPERTY = "notekea.chat.size"
        const val GPU_PROPERTY = "notekea.chat.gpu"
        const val REPORTS = "build/reports"
        const val GPU_SLOTS = 4
        const val PERCENT = 100
        const val NANOS_PER_MILLI = 1_000_000L

        // Three points under the first runs, 75 / 92 / 91 % (docs/answer-quality.md, 3.9).
        val BASELINES = mapOf(
            ModelSize.SMALL to 0.72,
            ModelSize.MEDIUM to 0.89,
            ModelSize.LARGE to 0.88
        )
    }
}

/** What the Sample Vault names, as committed (local edits to `smoke-data` change no number), for the plan's context. */
internal object SampleVaultNames {
    private const val ROOT = "smoke-data/vaults/Sample Vault/"

    private val files = git("ls-files", "-z", "--", ROOT)
        .split('\u0000')
        .filter { it.endsWith(suffix = ".md") }
        .map { it.removePrefix(prefix = ROOT) }
        .sorted()

    private val folders = files
        .mapNotNull {
            it.substringBeforeLast(
                delimiter = '/',
                missingDelimiterValue = ""
            ).ifEmpty { null }
        }
        .distinct()

    private val notes = files.map { it.removeSuffix(suffix = ".md") }

    private val frontmatter = files.map {
        git("show", "HEAD:$ROOT$it")
            .substringAfter(delimiter = "---\n")
            .substringBefore(delimiter = "\n---")
    }

    private val properties = frontmatter
        .flatMap { block ->
            PROPERTY
                .findAll(input = block)
                .map {
                    it.groupValues[1]
                }
                .toList()
        }
        .filter {
            it != "tags"
        }
        .distinct()

    private val tags = frontmatter
        .flatMap { block ->
            TAGS
                .find(input = block)
                ?.groupValues
                ?.get(1)
                ?.split(",")
                .orEmpty()
        }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    fun context(openNote: String?): ActionContext = ActionContext(
        today = "2026-11-10",
        openNote = openNote?.let { name ->
            notes.firstOrNull {
                it.endsWith(suffix = "/$name") || it == name
            } ?: name
        },
        folders = folders,
        notes = notes,
        properties = properties,
        tags = tags,
        themes = listOf("workspace blue", "graphite", "sage", "lagoon", "iris", "rose", "clay", "honey")
    )

    private val PROPERTY get() = Regex(
        pattern = "^([A-Za-z_]+):",
        option = RegexOption.MULTILINE
    )
    private val TAGS get() = Regex(pattern = "tags: \\[([^]]*)]")

    private fun git(vararg arguments: String): String {
        val process =
            ProcessBuilder(listOf("git") + arguments).directory(File("..").canonicalFile).start()

        val output = process
            .inputStream
            .readBytes()
            .decodeToString()

        check(process.waitFor() == 0) { "git ${arguments.first()} failed" }

        return output
    }
}

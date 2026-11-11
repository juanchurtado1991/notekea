package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ActionContext
import app.notekea.chat.domain.ActionField
import app.notekea.chat.domain.PlanRequestUseCase
import app.notekea.chat.domain.PlannedStep
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
        modelPath = folder.resolve(FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve(RERANKER_FILE).toString(),
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

        val steps = (
            plan(
                request = asked,
                context = SampleVaultNames.context(openNote = open)
            ) as? Outcome.Success
            )
            ?.value
            .orEmpty()

        val millis = (System.nanoTime() - start) / NANOS_PER_MILLI
        val expected = request.get("steps").toList()

        val merged = merged(steps = steps)

        val isRight = merged.size == expected.size &&
            merged.zip(expected).all { (step, wanted) ->
                matches(
                    step = step,
                    wanted = wanted
                )
            }

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

    /** Steps of one action in a row, each on one note, as one step on all of them: the same plan. */
    private fun merged(steps: List<PlannedStep>): List<PlannedStep> = steps
        .fold(emptyList()) { kept, step ->
            val last = kept.lastOrNull()

            val isSame =
                last != null &&
                    last.kind == step.kind &&
                    last.kind.isConfirmed &&
                    last.fields - ActionField.NOTE == step.fields - ActionField.NOTE

            if (!isSame) {
                kept + step
            } else {
                kept.dropLast(n = 1) + last.copy(
                    fields = last.fields - ActionField.NOTE,
                    notes = last.notes +
                        listOfNotNull(last[ActionField.NOTE], step[ActionField.NOTE]) +
                        step.notes
                )
            }
        }

    private fun matches(
        step: PlannedStep,
        wanted: com.fasterxml.jackson.databind.JsonNode
    ): Boolean {
        if (step.kind.written != wanted.get("action").asText()) return false

        return wanted
            .get("fields")
            .properties()
            .all { (name, value) ->
                val field = ActionField.entries.first {
                    it.written == name
                }

                val listed = if (field == ActionField.NOTE) step.notes else emptyList()
                val planned = listOfNotNull(step[field]) + listed

                val expected = value.asText().lowercase()

                // A folder's notes, listed one by one, are that folder.
                val isFolderListed =
                    field == ActionField.FOLDER &&
                        step.notes.isNotEmpty() &&
                        step.notes.all { it.lowercase().startsWith(prefix = "$expected/") }

                isFolderListed || planned.any { expected.isEmpty() || expected in it.lowercase() }
            }
    }

    private companion object {
        const val SET_FILE = "../smoke-data/eval/actions.json"
        const val MODELS_PROPERTY = "notekea.chat.models"
        const val SIZE_PROPERTY = "notekea.chat.size"
        const val GPU_PROPERTY = "notekea.chat.gpu"
        const val RERANKER_FILE = "bge-reranker-v2-m3-Q4_K_M.gguf"
        const val REPORTS = "build/reports"
        const val GPU_SLOTS = 4
        const val PERCENT = 100
        const val NANOS_PER_MILLI = 1_000_000L
        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )

        // Three points under the first runs, 75 / 92 / 91 % (docs/answer-quality.md, 3.9).
        val BASELINES = mapOf(
            ModelSize.SMALL to 0.72,
            ModelSize.MEDIUM to 0.89,
            ModelSize.LARGE to 0.88
        )
    }
}

/** What the Sample Vault names, as committed (local edits to `smoke-data` change no number), for the plan's context. */
private object SampleVaultNames {
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

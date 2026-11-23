package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.domain.loop.TurnRouter
import app.notekea.chat.domain.loop.WorkKind
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

/**
 * The loop's router on the action evaluation's 126 requests (`docs/agent-loop.md`, step 6): each routed to the kind
 * of work whose tools can do its first action. Run by hand: `./gradlew :app:chatModelTest -PchatModels=<folder>
 * -PchatSize=medium --tests '*RouterEval*'`; the report is `build/reports/router-eval-<size>.md`.
 */
class RouterEvalChatModelTest {
    private val folder = Path.of(System.getProperty(MODELS_PROPERTY).orEmpty())
    private val size = ModelSize.fromId(id = System.getProperty(SIZE_PROPERTY)) ?: ModelSize.SMALL

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(EvalModels.FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve(EvalModels.RERANKER_FILE).toString(),
        slots = SLOTS,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
        dispatchers = JvmAppDispatchers()
    )

    private val router = TurnRouter(llmEngine = engine)

    @AfterTest
    fun tearDown() {
        engine.close()
    }

    @Test
    fun `the router sends each request of the action evaluation to work whose tools can do it`() =
        runBlocking {
            assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))

            val requests = ObjectMapper()
                .readTree(File(SET_FILE))
                .toList()
                .filter {
                    it.get("steps").size() > 0
                }

            val slots = Semaphore(permits = SLOTS)

            val rows = coroutineScope {
                requests
                    .map { request ->
                        async {
                            slots.withPermit {
                                val asked = request.get("request").asText()

                                val action = request
                                    .get("steps")[0]
                                    .get("action")
                                    .asText()

                                val kind = router.route(
                                    task = asked,
                                    skills = emptyList()
                                ).kind

                                Triple(asked, action, kind)
                            }
                        }
                    }
                    .awaitAll()
            }

            val graded = rows.filter {
                it.second in WANTED
            }

            val right = graded.count { (_, action, kind) ->
                kind in WANTED.getValue(action)
            }

            val report = buildString {
                appendLine("# The router on the action evaluation (${size.id})")
                appendLine()
                appendLine("$right of ${graded.size} routed right (refusals not graded)")
                appendLine()

                rows.forEach { (asked, action, kind) ->
                    val mark = when {
                        action !in WANTED -> "·"
                        kind in WANTED.getValue(action) -> "✓"
                        else -> "✗"
                    }

                    appendLine("- $mark $action → ${kind.written}: ${asked.lines().first().take(n = 90)}")
                }
            }

            File(REPORTS, "router-eval-${size.id}.md").run {
                parentFile.mkdirs()
                writeText(text = report)
            }

            println(report.lines()[2])
        }

    private companion object {
        const val SET_FILE = "../smoke-data/eval/actions.json"
        const val MODELS_PROPERTY = "notekea.chat.models"
        const val SIZE_PROPERTY = "notekea.chat.size"
        const val REPORTS = "build/reports"
        const val SLOTS = 4

        private val WRITES = setOf(WorkKind.WRITE, WorkKind.ORGANIZE)
        private val ORGANIZES = setOf(WorkKind.ORGANIZE)
        private val SHOWS = setOf(WorkKind.APP)

        // Sets with find and tasks: ask and app answer with what they find, organize acts on it.
        private val LISTS = setOf(WorkKind.ASK, WorkKind.APP)

        // The kinds whose tools can do each first action; a refusal is not graded, the loop has none yet. A search
        // may be answered with the notes found as well as shown in the app's search; app has help's tool.
        val WANTED: Map<String, Set<WorkKind>> =
            listOf("answer").associateWith { setOf(WorkKind.ASK) } +
                listOf("find_notes", "show_tasks", "list_untagged", "list_broken_links")
                    .associateWith { LISTS } +
                listOf("write_from_notes", "create_note", "append_text").associateWith { WRITES } +
                listOf("make_cards", "notes_from_text").associateWith { setOf(WorkKind.WRITE) } +
                listOf(
                    "set_property", "add_tag", "add_task", "complete_task", "link_notes", "move", "rename",
                    "create_folder", "bookmark", "trash"
                ).associateWith { ORGANIZES } +
                listOf(
                    "open_note",
                    "open_today",
                    "show_panel",
                    "split_pane",
                    "close_other_tabs",
                    "toggle_source",
                    "set_theme"
                ).associateWith { SHOWS } +
                mapOf(
                    "search" to SHOWS + WorkKind.ASK,
                    "clip_url" to setOf(WorkKind.WEB),
                    "chat" to setOf(WorkKind.TALK),
                    "help" to setOf(WorkKind.HELP, WorkKind.APP),
                    "overview" to setOf(WorkKind.OVERVIEW)
                )
    }
}

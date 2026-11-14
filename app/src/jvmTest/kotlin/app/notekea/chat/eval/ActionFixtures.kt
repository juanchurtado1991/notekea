package app.notekea.chat.eval

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.LlmEngine
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

/**
 * The evaluation's requests with what the Medium model replied to each, word for word (`docs/agent-loop.md`, step
 * 0): replayed, they check everything that turns a reply into a plan with no model, in `check`.
 * [ActionFixturesChatModelTest] records them, [ActionFixturesTest] replays them.
 */
internal object ActionFixtures {
    const val REQUEST = "request"
    const val REPLIES = "replies"
    const val RIGHT = "right"

    private const val SET_FILE = "../smoke-data/eval/actions.json"
    private val json = ObjectMapper()

    val file = File("../smoke-data/eval/fixtures/actions-medium.json")

    /** The requests of the evaluation set, with the plan each expects. */
    fun requests(): List<JsonNode> = json.readTree(File(SET_FILE)).toList()

    /** The fixtures as recorded, one for each request of the set. */
    fun recorded(): List<JsonNode> = json.readTree(file).toList()

    /** Whether [request] planned through [engine] gives the plan it expects. */
    suspend fun isRight(
        request: JsonNode,
        engine: LlmEngine
    ): Boolean {
        val open = request
            .get("open")
            ?.takeUnless { it.isNull }
            ?.asText()

        val plan = PlanRequestUseCase(
            llmEngine = engine,
            planReader = GhostActionPlanReader()
        )

        val steps = (
            plan(
                request = request.get(REQUEST).asText(),
                context = SampleVaultNames.context(openNote = open)
            ) as? Outcome.Success
            )
            ?.value
            .orEmpty()

        return ActionGrade.isRight(
            steps = steps,
            expected = request.get("steps").toList()
        )
    }

    /** [fixtures] written to [file], a request a line so a new recording shows what changed. */
    fun written(fixtures: List<JsonNode>) {
        file.parentFile.mkdirs()

        file.writeText(
            text = fixtures.joinToString(
                separator = ",\n",
                prefix = "[\n",
                postfix = "\n]\n"
            ) { json.writeValueAsString(it) }
        )
    }

    /** A fixture: what was asked, every reply the model gave for it, in order, and whether the plan was right. */
    fun fixture(
        request: JsonNode,
        replies: List<String>,
        isRight: Boolean
    ): JsonNode {
        val fixture = json.createObjectNode()

        fixture.put(REQUEST, request.get(REQUEST).asText())
        replies.forEach { fixture.withArray(REPLIES).add(it) }
        fixture.put(RIGHT, isRight)

        return fixture
    }
}

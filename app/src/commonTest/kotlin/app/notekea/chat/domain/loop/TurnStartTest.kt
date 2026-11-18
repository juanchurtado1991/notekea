package app.notekea.chat.domain.loop

import app.notekea.chat.domain.skill.Skill
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.web.domain.SourceTrust
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How a turn starts (the bug hunt of 2026-11-17): the router's schema with a skill named with a quote (E12), the
 * vault's own web sources trusted (E7), and a turn that writes framed as a change (the frame alone, 2026-11-18).
 */
class TurnStartTest {
    private val engine = FakeLlmEngine()

    @Test
    fun `a skill named with a quote and a backslash is escaped in the schema, and its name fits the reply`() =
        runTest {
            // Longer than the router's reply was given room for: the reply was cut and read as no route.
            val long = " of writing a note".repeat(n = 20)
            val name = "Sam's \"best\" way\\$long"
            val written = "\"Sam's \\\"best\\\" way\\\\$long\""

            engine.reply = { "{\"work\":\"write\",\"skill\":$written}" }

            val route = TurnRouter(llmEngine = engine).route(
                task = "write a note about soil",
                skills = listOf(
                    Skill(
                        name = name,
                        description = "A way.",
                        instructions = ""
                    )
                )
            )

            val options = engine.options.single()

            assertEquals(
                expected = listOf(true, true, name),
                actual = listOf(
                    written in options.jsonSchema.orEmpty(),
                    options.maxTokens > engine.countTokens(text = written),
                    route.skill?.name
                )
            )
        }

    @Test
    fun `the vault's own web sources are trusted`() {
        val turn = TurnSetup.contextOf(
            task = "how is compost made?",
            vaultPath = VAULT,
            notes = emptyList(),
            today = "2026-11-17",
            remembered = emptyList(),
            openNote = null,
            sources = listOf("fao.org")
        )

        assertEquals(
            expected = SourceTrust.Trust.TRUSTED,
            actual = turn.trust.of(domain = "www.fao.org")
        )
    }

    @Test
    fun `a turn that writes is framed as a change, as one that organizes, and a question is not`() =
        runTest {
            val asked = mutableListOf<Boolean>()

            val frames = FrameSource { _, _, _, _, isChange, _ ->
                asked += isChange
                null
            }

            listOf(WorkKind.WRITE, WorkKind.ORGANIZE, WorkKind.ASK).forEach { kind ->
                TurnSetup.frameOf(
                    task = "add a misc line of 200 to the budget",
                    vaultPath = VAULT,
                    notes = emptyList(),
                    memory = emptyList(),
                    kind = kind,
                    frames = frames,
                    openNote = null
                )
            }

            assertEquals(
                expected = listOf(true, true, false),
                actual = asked
            )
        }
}

package app.notekea.chat.data

import app.notekea.query.data.GhostQueryJsonReader
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the model may hand back that is not a plan, a query or a move: cut off at the token limit or on Stop, a field
 * of the wrong type, plain text. Each is read as no reply, never as a crash.
 */
class GhostActionPlanReaderTest {
    private val reader = GhostActionPlanReader()
    private val queries = GhostQueryJsonReader()

    @Test
    fun `a reply that is not the JSON asked for is read as none`() {
        val broken = listOf(
            "{\"steps\":[{\"action\":\"create_note\",\"text\":\"Half of it",
            "Sure! Here is the plan.",
            ""
        )

        val wrongPlans = listOf("{\"steps\":\"create_note\"}", "{\"steps\":[{\"action\":3}]}")

        val read = broken.map {
            listOf(
                reader.read(json = it),
                reader.readNotes(json = it),
                reader.readMove(json = it),
                queries.read(it)
            )
        } + wrongPlans.map { listOf(reader.read(json = it)) }

        assertEquals(
            expected = broken.map {
                listOf(null, null, null, null)
            } + wrongPlans.map { listOf(null) },
            actual = read
        )
    }

    @Test
    fun `a plan as asked is read`() {
        val steps =
            reader.read(json = "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Groceries\"}]}")

        assertEquals(
            expected = listOf("Groceries"),
            actual = steps?.map {
                it
                    .fields
                    .values
                    .single()
            }
        )
    }
}

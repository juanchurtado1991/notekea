package app.notekea.chat.data

import app.notekea.query.data.GhostQueryJsonReader
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the model may hand back that is not the notes or the query asked for: cut off at the token limit or on Stop,
 * plain text. Each is read as no reply, never as a crash.
 */
class GhostTextNotesReaderTest {
    private val reader = GhostTextNotesReader()
    private val queries = GhostQueryJsonReader()

    @Test
    fun `a reply that is not the JSON asked for is read as none`() {
        val broken = listOf(
            "{\"notes\":[{\"title\":\"Groceries\",\"text\":\"Half of it",
            "Sure! Here are the notes.",
            ""
        )

        assertEquals(
            expected = broken.map { listOf(null, null) },
            actual = broken.map {
                listOf(
                    reader.readNotes(json = it),
                    queries.read(it)
                )
            }
        )
    }

    @Test
    fun `the notes as asked are read, one without a text left out`() {
        assertEquals(
            expected = listOf("Groceries" to "Milk and eggs."),
            actual = reader.readNotes(
                json = "{\"notes\":[{\"title\":\" Groceries \",\"text\":\"Milk and eggs.\"}," +
                    "{\"title\":\"Empty\",\"text\":\" \"}]}"
            )
        )
    }
}

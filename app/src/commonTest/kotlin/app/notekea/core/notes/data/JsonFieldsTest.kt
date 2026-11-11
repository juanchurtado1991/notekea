package app.notekea.core.notes.data

import kotlin.test.Test
import kotlin.test.assertEquals

/** Settings → Files sets one field of Obsidian's file and keeps the rest as written (5.2, 2026-11-11). */
class JsonFieldsTest {
    @Test
    fun `a field is replaced in place, added at the end, or written in a new object, the rest kept`() {
        val obsidian =
            "{\n  \"alwaysUpdateLinks\": true,\n  \"attachmentFolderPath\": \"Assets\",\n  \"theme\": \"moonstone\"\n}"

        assertEquals(
            expected = listOf(
                "{\n  \"alwaysUpdateLinks\": true,\n  \"attachmentFolderPath\": \"files/img\",\n  \"theme\": \"moonstone\"\n}",
                "{\n  \"theme\": \"moonstone\",\n  \"attachmentFolderPath\": \"a \\\"b\\\"\"\n}",
                "{\n  \"folder\": \"Journal\"\n}",
                "{\n  \"folder\": \"Journal\"\n}"
            ),
            actual = listOf(
                JsonFields.withText(
                    json = obsidian,
                    name = "attachmentFolderPath",
                    value = "files/img"
                ),
                JsonFields.withText(
                    json = "{\n  \"theme\": \"moonstone\"\n}",
                    name = "attachmentFolderPath",
                    value = "a \"b\""
                ),
                JsonFields.withText(
                    json = "",
                    name = "folder",
                    value = "Journal"
                ),
                JsonFields.withText(
                    json = "{}",
                    name = "folder",
                    value = "Journal"
                )
            )
        )
    }
}

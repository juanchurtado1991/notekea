package app.notekea.notes.domain

import app.notekea.core.domain.time.CalendarDate
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The properties cases of `MVP_SMOKE_TEST.md` (1.14) on the smoke data itself: `Properties practice` reads one property
 * of each type, the edits of the cases change only their own lines, and `Broken properties` is not valid YAML.
 */
class SmokePropertiesTest {
    private val reader = FrontmatterReader()
    private val patch = FrontmatterPatch()
    private val header = NoteDocument.fromText(text = File(PRACTICE).readText()).header

    @Test
    fun `1_14-01 Properties practice reads one property of each type, and Broken properties is invalid`() {
        val broken = NoteDocument.fromText(text = File(BROKEN).readText()).header

        assertEquals(
            expected = listOf(
                "status" to PropertyValue.Text(text = "draft"),
                "rating" to PropertyValue.Number(text = "4"),
                "done" to PropertyValue.Checkbox(isChecked = false),
                "due" to PropertyValue.Date(date = CalendarDate(year = 2026, month = 11, day = 2)),
                "tags" to PropertyValue.Items(items = listOf("recipe", "weekend")),
                "aliases" to PropertyValue.Items(items = listOf("Practice", "Form test")),
                "source" to PropertyValue.Other(source = "source:\n  book: Abuela's notebook\n  page: 12")
            ) to FrontmatterContent.Invalid(line = 2),
            actual = (reader.read(header = header) as FrontmatterContent.Properties).properties
                .map { it.key to it.value } to reader.read(header = broken)
        )
    }

    @Test
    fun `1_14-02 each edit of the case changes only its own line`() {
        val edits = listOf(
            "status" to PropertyValue.Text(text = "ready"),
            "rating" to PropertyValue.Number(text = "5"),
            "done" to PropertyValue.Checkbox(isChecked = true),
            "due" to PropertyValue.Date(date = CalendarDate(year = 2026, month = 11, day = 9)),
            "tags" to PropertyValue.Items(items = listOf("recipe", "weekend", "brunch")),
            "aliases" to PropertyValue.Items(items = listOf("Form test", "Notekea"))
        )
        val edited = edits.fold(initial = header) { text, (key, value) ->
            patch.set(header = text, key = key, value = value)
        }

        assertEquals(
            expected = header
                .replace("status: draft", "status: ready")
                .replace("rating: 4", "rating: 5")
                .replace("done: false", "done: true")
                .replace("due: 2026-11-02", "due: 2026-11-09")
                .replace("  - weekend\n", "  - weekend\n  - brunch\n")
                .replace("aliases: [Practice, 'Form test']", "aliases: ['Form test', Notekea]"),
            actual = edited
        )
    }

    companion object {
        private const val BROKEN = "../smoke-data/notes/Broken properties.md"
        private const val PRACTICE = "../smoke-data/notes/Properties practice.md"
    }
}

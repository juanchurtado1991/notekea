package app.notekea.notes.domain

import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Note info case of `MVP_SMOKE_TEST.md` (1.14) on the smoke data itself: what `Tasks and callouts` holds. */
class SmokeNoteInfoTest {
    @Test
    fun `1_14-08 Note info counts the words, links, tags and tasks of Tasks and callouts`() {
        val document = NoteDocument.fromText(text = File(NOTE).readText())
        val properties = FrontmatterReader().read(header = document.header) as FrontmatterContent.Properties
        val tags = properties.properties.filter { PropertyKeys.isTags(key = it.key) }
            .flatMap { (it.value as PropertyValue.Items).items }

        assertEquals(
            expected = NoteStats(words = 236, links = 0, tags = 2, tasks = 9, doneTasks = 4),
            actual = GetNoteStatsUseCase(linkFinder = JetBrainsMarkdownLinkFinder())(
                body = document.body,
                propertyTags = tags
            )
        )
    }

    companion object {
        private const val NOTE = "../smoke-data/notes/Tasks and callouts.md"
    }
}

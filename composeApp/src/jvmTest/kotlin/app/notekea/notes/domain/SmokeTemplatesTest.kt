package app.notekea.notes.domain

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.notes.data.ObsidianVaultSettingsReader
import app.notekea.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The templates cases of `MVP_SMOKE_TEST.md` (1.12) on the smoke data itself: the Playground Vault without a templates
 * folder, and the Templates Vault's Obsidian settings, templates and notes, so the guide never promises a template, a
 * date or a property the data does not give.
 */
class SmokeTemplatesTest {
    private val testDispatcher = StandardTestDispatcher()
    private val reader = ObsidianVaultSettingsReader(
        fileSystem = FileSystem.SYSTEM,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )
    private val templates = Templates()
    private val moment = LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 27), hour = 14, minute = 5)

    @Test
    fun `1_12-01 the Playground Vault has no templates folder and no Obsidian templates settings`() =
        runTest(context = testDispatcher) {
            assertEquals(
                expected = null to TemplateListing.NoFolder,
                actual = reader.templates(vaultPath = PERSONAL_NOTES) to
                    templates.listing(tree = treeOf(folder = PERSONAL_NOTES), settings = TemplateSettings.DEFAULT)
            )
        }

    @Test
    fun `1_12-02 Snippets' templates are listed, and Meeting goes into Kickoff notes with its properties`() =
        runTest(context = testDispatcher) {
            val settings = reader.templates(vaultPath = TEMPLATES_VAULT)!!
            val listing = templates.listing(tree = treeOf(folder = TEMPLATES_VAULT), settings = settings)
            val note = NoteDocument.fromText(text = read(path = "Projects/Kickoff notes.md"))

            val result = insert(note = note, title = "Kickoff notes", settings = settings)

            assertEquals(
                expected = listOf(
                    listOf("Meeting", "Work/Weekly review"),
                    "---\ntags:\n  - project\n  - meeting\nstatus: active\ntype: meeting\n---\n",
                    "# Kickoff notes\n\nGoals for the launch.\n\nNotes from the call:\n# Kickoff notes\n\n" +
                        MEETING_BODY
                ),
                actual = listOf(
                    (listing as TemplateListing.Found).templates.map {
                        templates.nameOf(template = it, settings = settings)
                    },
                    result.first,
                    result.second
                )
            )
        }

    @Test
    fun `1_12-03 the empty note becomes Meeting whole, titled with its name`() = runTest(context = testDispatcher) {
        val settings = reader.templates(vaultPath = TEMPLATES_VAULT)!!

        val result = insert(
            note = NoteDocument.fromText(text = read(path = "Projects/Empty note.md")),
            title = "Empty note",
            settings = settings
        )

        assertEquals(
            expected = "---\ntags: [meeting]\ntype: meeting\n---\n" to "# Empty note\n\n$MEETING_BODY",
            actual = result
        )
    }

    /** Meeting filled in for [title] at 14:05 on Tuesday, October 27th 2026, inserted at the end of [note]. */
    private fun insert(
        note: NoteDocument,
        title: String,
        settings: TemplateSettings
    ): Pair<String, String> {
        val filled = TemplateFiller().fill(
            template = read(path = "Snippets/Meeting.md"),
            moment = moment,
            title = title,
            dateFormat = settings.dateFormat,
            timeFormat = settings.timeFormat
        )
        val end = note.body.length
        val result = TemplateInsertion().insert(
            note = note,
            selectionStart = end,
            selectionEnd = end,
            template = NoteDocument.fromText(text = filled)
        )
        val edit = result.edit
        return result.header to
            note.body.replaceRange(startIndex = edit.start, endIndex = edit.end, replacement = edit.replacement)
    }

    private fun read(
        path: String
    ): String = File(TEMPLATES_VAULT, path).readText()

    /** The notes and folders of the vault at [folder], hidden folders left out, as the tree lists them. */
    private fun treeOf(
        folder: String
    ): VaultTree = File(folder).walkTopDown()
        .filter { it.isFile && it.name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION) }
        .map { it.relativeTo(base = File(folder)).invariantSeparatorsPath }
        .filterNot { path -> path.split("/").any { it.startsWith(".") } }
        .fold(initial = VaultTree.EMPTY) { tree, path -> tree.withNote(note = entry(path = path)) }

    companion object {
        private const val MEETING_BODY =
            "Date: 27/10/2026 (Tuesday) at 2:05 PM\n\n## Attendees\n- \n\n## Action items\n- [ ] \n"
        private const val PERSONAL_NOTES = "../smoke-data/vaults/Personal Notes"
        private const val TEMPLATES_VAULT = "../smoke-data/templates"
    }
}

package app.notekea.notes.domain

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import app.notekea.notes.data.ObsidianVaultSettingsReader
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The daily notes cases of `MVP_SMOKE_TEST.md` (1.10) on the smoke data itself: the Playground Vault's journal under
 * the defaults, and the Daily Vault's Obsidian settings, notes and template, so the guide never promises a name, a
 * neighbor or a filled-in line the data does not give.
 */
class SmokeDailyNotesTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dailyNotes = DailyNotes()

    @Test
    fun `1_10-03 the journal's notes are daily notes under the defaults, each next to its neighbors`() {
        val notes = notesOf(folder = PERSONAL_NOTES)

        assertEquals(
            expected = listOf(null, "Journal/2026-10-20.md", "Journal/2026-10-21.md") to
                listOf("Journal/2026-10-21.md", "Journal/2026-10-22.md", null),
            actual = JOURNAL.map {
                dailyNotes.neighborsOf(path = it, notes = notes, settings = DailyNoteSettings.DEFAULT)
            }.let { found -> found.map { it?.previous?.value } to found.map { it?.next?.value } }
        )
    }

    @Test
    fun `1_10-04 the Daily Vault's settings name its notes, Ideas is not one, and the template fills in`() =
        runTest(context = testDispatcher) {
            val reader = ObsidianVaultSettingsReader(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
            )
            val settings = reader.dailyNotes(vaultPath = DAILY_VAULT)!!
            val notes = notesOf(folder = DAILY_VAULT)
            val today = CalendarDate(year = 2026, month = 10, day = 27)
            val template = File(DAILY_VAULT, settings.template + RelativePath.MARKDOWN_EXTENSION).readText()

            assertEquals(
                expected = listOf(
                    "Diary/2026/October/Tuesday, October 27th 2026.md",
                    "Diary/2026/October/Thursday, October 22nd 2026.md",
                    null,
                    "# Tuesday, October 27th 2026\n\nCreated at 09:15.\n\n## Plan for Tuesday\n\n- [ ] \n"
                ),
                actual = listOf(
                    dailyNotes.pathOf(date = today, settings = settings).value,
                    dailyNotes.neighborsOf(
                        path = dailyNotes.pathOf(date = today, settings = settings),
                        notes = notes + dailyNotes.pathOf(date = today, settings = settings),
                        settings = settings
                    )?.previous?.value,
                    dailyNotes.dateOf(path = RelativePath(value = "Diary/Ideas.md"), settings = settings),
                    DailyNoteTemplate().fill(
                        template = template,
                        moment = LocalMoment(date = today, hour = 9, minute = 15),
                        title = "Tuesday, October 27th 2026",
                        dateFormat = settings.format
                    )
                )
            )
        }

    /** Every note of the vault at [folder], hidden folders left out, as the tree lists them. */
    private fun notesOf(
        folder: String
    ): List<RelativePath> = File(folder).walkTopDown()
        .filter { it.isFile && it.name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION) }
        .map { it.relativeTo(base = File(folder)).invariantSeparatorsPath }
        .filterNot { path -> path.split("/").any { it.startsWith(".") } }
        .map { RelativePath(value = it) }
        .toList()

    companion object {
        private const val DAILY_VAULT = "../smoke-data/daily"
        private const val PERSONAL_NOTES = "../smoke-data/vaults/Personal Notes"
        private val JOURNAL = listOf("Journal/2026-10-20.md", "Journal/2026-10-21.md", "Journal/2026-10-22.md")
            .map { RelativePath(value = it) }
    }
}

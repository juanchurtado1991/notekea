package app.notekea.notes.data

import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.DailyNoteSettings
import app.notekea.notes.domain.FollowedLink
import app.notekea.notes.domain.OpenDailyNoteUseCase
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.nio.file.Files
import java.time.ZoneId
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Daily notes on real files (`ROADMAP.md` 1.10): Obsidian's `daily-notes.json` read from a temporary vault, and today's
 * note created there by the real adapters, in a real time zone.
 */
@OptIn(ExperimentalPathApi::class)
class DailyNotesOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault = Files.createTempDirectory(TEMP_PREFIX)
    private val vaultPath = vault.toString()
    private val settings = ObsidianVaultSettingsReader(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `daily-notes json is read as Obsidian writes it, blank fields and a missing file falling back`() =
        runTest(context = testDispatcher) {
            val missing = settings.dailyNotes(vaultPath = vaultPath)
            vault.resolve(".obsidian").createDirectories()
            vault.resolve(".obsidian/daily-notes.json").writeText(
                text = """{"folder":"/Diary/","format":"DD-MM-YYYY","template":"Templates/Day.md","autorun":false}"""
            )
            val full = settings.dailyNotes(vaultPath = vaultPath)
            vault.resolve(".obsidian/daily-notes.json").writeText(text = """{"folder":"","format":""}""")
            val blank = settings.dailyNotes(vaultPath = vaultPath)
            vault.resolve(".obsidian/daily-notes.json").writeText(text = "{not json")

            assertEquals(
                expected = listOf(
                    null,
                    DailyNoteSettings(folder = "Diary", format = "DD-MM-YYYY", template = "Templates/Day"),
                    DailyNoteSettings(folder = "", format = "YYYY-MM-DD", template = null),
                    null
                ),
                actual = listOf(missing, full, blank, settings.dailyNotes(vaultPath = vaultPath))
            )
        }

    @Test
    fun `today's note is created from the template in the user's time zone, in folders made for it`() =
        runTest(context = testDispatcher) {
            vault.resolve(".obsidian").createDirectories()
            vault.resolve(".obsidian/daily-notes.json").writeText(
                text = """{"folder":"Journal","format":"YYYY/YYYY-MM-DD","template":"Templates/Day"}"""
            )
            vault.resolve("Templates").createDirectories()
            vault.resolve("Templates/Day.md").writeText(text = "# {{date:dddd, MMMM Do}}\n")
            val tokyo = openDailyNote(zone = "Asia/Tokyo")

            val opened = tokyo(vaultPath = vaultPath, date = null)

            assertEquals(
                expected = Outcome.Success(
                    value = FollowedLink(path = RelativePath(value = "Journal/2026/2026-10-27.md"), isNew = true)
                ) to "# Tuesday, October 27th\n",
                actual = opened to vault.resolve("Journal/2026/2026-10-27.md").readText()
            )
        }

    private fun openDailyNote(
        zone: String
    ): OpenDailyNoteUseCase = OpenDailyNoteUseCase(
        vaultSettingsReader = settings,
        noteFileOperations = OkioNoteFileOperations(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers),
        noteContentStore = OkioNoteContentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers),
        localCalendar = JvmLocalCalendar(zone = { ZoneId.of(zone) }),
        clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T20:00:00Z"))
    )

    companion object {
        private const val TEMP_PREFIX = "notekea-daily"
    }
}

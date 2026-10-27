package app.notekea.notes.data

import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.ApplyTemplateUseCase
import app.notekea.notes.domain.CreateTemplatesFolderUseCase
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TemplateSettings
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
 * Templates on real files (`ROADMAP.md` 1.12): Obsidian's `templates.json` read from a temporary vault, the templates
 * folder made with its example, and a note filled from a template by the real adapters, in a real time zone.
 */
@OptIn(ExperimentalPathApi::class)
class TemplatesOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault = Files.createTempDirectory(TEMP_PREFIX)
    private val vaultPath = vault.toString()
    private val settings = ObsidianVaultSettingsReader(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)
    private val operations = OkioNoteFileOperations(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)
    private val store = OkioNoteContentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `templates json is read as Obsidian writes it, blank fields and a missing file falling back`() =
        runTest(context = testDispatcher) {
            val missing = settings.templates(vaultPath = vaultPath)
            vault.resolve(".obsidian").createDirectories()
            vault.resolve(".obsidian/templates.json").writeText(
                text = """{"folder":"/Meta/Templates/","dateFormat":"DD.MM.YYYY","timeFormat":"h:mm A"}"""
            )
            val full = settings.templates(vaultPath = vaultPath)
            vault.resolve(".obsidian/templates.json").writeText(text = """{"folder":""}""")
            val blank = settings.templates(vaultPath = vaultPath)

            assertEquals(
                expected = listOf(
                    null,
                    TemplateSettings(folder = "Meta/Templates", dateFormat = "DD.MM.YYYY", timeFormat = "h:mm A"),
                    TemplateSettings(folder = "", dateFormat = "YYYY-MM-DD", timeFormat = "HH:mm")
                ),
                actual = listOf(missing, full, blank)
            )
        }

    @Test
    fun `the folder is made with its example, and a new note is filled from it in the user's time zone`() =
        runTest(context = testDispatcher) {
            val example = CreateTemplatesFolderUseCase(
                vaultSettingsReader = settings,
                noteFileOperations = operations,
                noteContentStore = store
            )(vaultPath = vaultPath, exampleName = "Meeting notes", exampleText = EXAMPLE)
            vault.resolve("Kickoff.md").writeText(text = "")

            val applied = ApplyTemplateUseCase(fillTemplate = fillTemplate(), noteContentStore = store)(
                vaultPath = vaultPath,
                note = RelativePath(value = "Kickoff.md"),
                template = RelativePath(value = "Templates/Meeting notes.md")
            )

            assertEquals(
                expected = listOf(
                    Outcome.Success(value = RelativePath(value = "Templates/Meeting notes.md")),
                    Outcome.Success(value = Unit),
                    "---\ntags: meeting\n---\n# Kickoff\n2026-10-27 05:00\n"
                ),
                actual = listOf(example, applied, vault.resolve("Kickoff.md").readText())
            )
        }

    private fun fillTemplate(): FillTemplateUseCase = FillTemplateUseCase(
        vaultSettingsReader = settings,
        noteContentStore = store,
        localCalendar = JvmLocalCalendar(zone = { ZoneId.of("Asia/Tokyo") }),
        clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T20:00:00Z"))
    )

    companion object {
        private const val TEMP_PREFIX = "notekea-templates"

        // Written on Windows: the new note still uses one kind of line break.
        private const val EXAMPLE = "---\r\ntags: meeting\r\n---\r\n# {{title}}\r\n{{date}} {{time}}\r\n"
    }
}

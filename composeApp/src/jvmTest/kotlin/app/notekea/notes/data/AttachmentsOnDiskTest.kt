package app.notekea.notes.data

import app.notekea.core.data.JvmLocalTimeFormatter
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** Attachments on real files: a temporary vault with Obsidian's settings folder, written and read by the adapters. */
@OptIn(ExperimentalPathApi::class)
class AttachmentsOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault = Files.createTempDirectory(TEMP_PREFIX)
    private val vaultPath = vault.toString()
    private val store = OkioAttachmentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)
    private val settings = ObsidianVaultSettingsReader(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `files are written and copied into new folders, never over another, and listed without hidden ones`() =
        runTest(context = testDispatcher) {
            val outside = Files.createTempFile(TEMP_PREFIX, ".pdf").also { Files.write(it, PDF) }
            Files.createDirectories(vault.resolve(".obsidian"))
            Files.write(vault.resolve(".obsidian/workspace.json"), PDF)
            Files.write(vault.resolve("Welcome.md"), PDF)

            val written = store.write(vaultPath = vaultPath, path = IMAGE, bytes = PNG)
            val again = store.write(vaultPath = vaultPath, path = IMAGE, bytes = PDF)
            val copied = store.copy(vaultPath = vaultPath, absolutePath = outside.toString(), path = MENU)

            assertEquals(
                expected = listOf<Any>(
                    Outcome.Success(value = Unit),
                    NotesError.NameTaken(name = IMAGE.name),
                    Outcome.Success(value = Unit)
                ),
                actual = listOf(written, (again as Outcome.Failure).error, copied)
            )
            assertEquals(
                expected = listOf(PNG.toList(), PDF.toList()),
                actual = listOf(IMAGE, MENU).map {
                    (store.read(vaultPath = vaultPath, path = it) as Outcome.Success).value.toList()
                }
            )
            assertEquals(
                expected = listOf("Welcome.md", "attachments/Pasted image.png", "attachments/docs/menu.pdf"),
                actual = (store.files(vaultPath = vaultPath) as Outcome.Success).value.map { it.value }.sorted()
            )
            Files.delete(outside)
        }

    @Test
    fun `a path is inside the vault only below its folder`() {
        assertEquals(
            expected = listOf("Travel/lisbon.jpg", null, null),
            actual = listOf(
                vault.resolve("Travel/lisbon.jpg").toOkioPath().toString(),
                vault.parent.resolve("other.png").toOkioPath().toString(),
                vaultPath
            ).map { store.pathInVault(vaultPath = vaultPath, absolutePath = it)?.value }
        )
    }

    @Test
    fun `Obsidian's attachment folder setting is read among its other settings, and nothing without the file`() =
        runTest(context = testDispatcher) {
            val before = settings.attachmentFolderPath(vaultPath = vaultPath)
            Files.createDirectories(vault.resolve(".obsidian"))
            Files.writeString(vault.resolve(".obsidian/app.json"), APP_JSON)

            assertEquals(
                expected = null to "Assets/img",
                actual = before to settings.attachmentFolderPath(vaultPath = vaultPath)
            )
        }

    @Test
    fun `the local time is written as Obsidian names pasted images`() {
        assertEquals(
            expected = 14,
            actual = JvmLocalTimeFormatter().compact(instant = Instant.parse(input = "2026-10-26T14:30:05Z")).count {
                it.isDigit()
            }
        )
    }

    companion object {
        private const val APP_JSON =
            "{\n  \"alwaysUpdateLinks\": true,\n  \"attachmentFolderPath\": \"Assets/img\",\n" +
                "  \"showLineNumber\": false,\n" +
                "  \"promptDelete\": false,\n  \"newFileFolderPath\": \"Inbox\"\n}\n"
        private const val TEMP_PREFIX = "notekea-attachments"
        private val IMAGE = RelativePath(value = "attachments/Pasted image.png")
        private val MENU = RelativePath(value = "attachments/docs/menu.pdf")
        private val PDF = byteArrayOf(37, 80, 68, 70)
        private val PNG = byteArrayOf(-119, 80, 78, 71)
    }
}

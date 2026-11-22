package app.notekea.core.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/** A vault's `.obsidian` files are replaced whole or left as they were, nothing left beside. */
class ObsidianVaultSettingsWriterTest {
    private val testDispatcher = StandardTestDispatcher()
    private val disk = FakeFileSystem().apply {
        createDirectories(dir = FOLDER)
        write(file = FOLDER / APP_FILE) { writeUtf8(string = ORIGINAL) }
    }

    // The code review of 2026-11-22: a refused move left `app.json.notekea-tmp` beside the file.
    @Test
    fun `a write that fails leaves the settings file as it was and no temporary file behind`() =
        runTest(context = testDispatcher) {
            val outcome = writer(fileSystem = refusingMoves()).setAttachmentFolder(
                vaultPath = VAULT,
                folder = "Files"
            )

            assertEquals(
                expected = listOf(
                    Outcome.Failure(
                        error = NotesError.OperationFailed(path = ".obsidian/app.json")
                    ),
                    ORIGINAL,
                    listOf(APP_FILE)
                ),
                actual = listOf(
                    outcome,
                    disk.read(file = FOLDER / APP_FILE) { readUtf8() },
                    disk.list(dir = FOLDER).map { it.name }
                )
            )
        }

    // The code review of 2026-11-22: one refusal, as Windows gives while Obsidian reads, failed it.
    @Test
    fun `a move refused for a moment is tried again, and the field is set`() =
        runTest(context = testDispatcher) {
            val outcome = writer(fileSystem = refusingMoves(times = 2)).setAttachmentFolder(
                vaultPath = VAULT,
                folder = "Files"
            )

            assertEquals(
                expected = listOf(Outcome.Success(value = Unit), true, listOf(APP_FILE)),
                actual = listOf(
                    outcome,
                    "\"Files\"" in disk.read(file = FOLDER / APP_FILE) { readUtf8() },
                    disk.list(dir = FOLDER).map { it.name }
                )
            )
        }

    /** A file system that refuses the first [times] moves, every move unless told. */
    private fun refusingMoves(times: Int = Int.MAX_VALUE): FileSystem =
        object : ForwardingFileSystem(delegate = disk) {
            private var refused = 0

            override fun atomicMove(
                source: Path,
                target: Path
            ) {
                if (refused < times) {
                    refused++
                    throw IOException("File in use")
                }

                super.atomicMove(
                    source = source,
                    target = target
                )
            }
        }

    private fun writer(fileSystem: FileSystem) = ObsidianVaultSettingsWriter(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    private companion object {
        const val APP_FILE = "app.json"
        const val ORIGINAL = "{\n  \"attachmentFolderPath\": \"Attachments\"\n}"
        const val VAULT = "/home/ana/Notes"
        val FOLDER = VAULT.toPath() / ".obsidian"
    }
}

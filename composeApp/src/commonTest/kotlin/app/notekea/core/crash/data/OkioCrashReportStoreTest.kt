package app.notekea.core.crash.data

import app.notekea.core.crash.domain.CrashReportError
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class OkioCrashReportStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fileSystem = FakeFileSystem()
    private val store = OkioCrashReportStore(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
        appDataDirectory = APP_DATA
    )

    @Test
    fun `a report is written in the crashes folder and its path returned`() = runTest(context = testDispatcher) {
        val outcome = store.save(name = "crash-2026-10-26T10-15-30Z.txt", text = "Notekea crash report\n")
        val file = "$APP_DATA/crashes/crash-2026-10-26T10-15-30Z.txt".toPath()

        assertEquals(
            expected = Outcome.Success(value = file.toString()) to "Notekea crash report\n",
            actual = outcome to fileSystem.read(file = file) { readUtf8() }
        )
    }

    @Test
    fun `only the ten most recent reports are kept, other files are left alone`() = runTest(context = testDispatcher) {
        val folder = "$APP_DATA/crashes".toPath()
        fileSystem.createDirectories(dir = folder)
        fileSystem.write(file = folder / "notes.txt") { writeUtf8(string = "mine") }
        (1..12).forEach { day ->
            store.save(
                name = "crash-2026-10-${day.toString().padStart(length = 2, padChar = '0')}T00-00-00Z.txt",
                text = ""
            )
        }

        assertEquals(
            expected = listOf("crash-2026-10-03T00-00-00Z.txt", "crash-2026-10-12T00-00-00Z.txt", "notes.txt", 11),
            actual = fileSystem.list(dir = folder).map { it.name }.sorted().let { names ->
                listOf(names.first(), names[names.size - 2], names.last(), names.size)
            }
        )
    }

    @Test
    fun `a folder that cannot be written is reported`() = runTest(context = testDispatcher) {
        // A file where the app data folder should be: nothing can be created inside it.
        fileSystem.createDirectories(dir = APP_DATA.toPath().parent!!)
        fileSystem.write(file = APP_DATA.toPath()) { writeUtf8(string = "not a folder") }

        assertEquals(
            expected = Outcome.Failure(error = CrashReportError.WriteFailed),
            actual = store.save(name = "crash-2026-10-26T10-15-30Z.txt", text = "")
        )
    }

    companion object {
        private const val APP_DATA = "/home/ana/.local/share/notekea"
    }
}

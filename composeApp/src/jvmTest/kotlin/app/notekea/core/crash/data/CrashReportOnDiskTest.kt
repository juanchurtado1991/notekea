package app.notekea.core.crash.data

import app.notekea.core.crash.domain.RecordCrashUseCase
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.FakeClock
import app.notekea.core.platform.data.JvmSystemEnvironment
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/** A crash recorded with the production store and environment, on a real folder, as the app does when it fails. */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class CrashReportOnDiskTest {
    private val appData: Path = Files.createTempDirectory(TEMP_PREFIX)

    @AfterTest
    fun tearDown() {
        appData.deleteRecursively()
    }

    @Test
    fun `a crash report is written to the app data folder with a name every OS accepts`() = runBlocking {
        val recordCrash = RecordCrashUseCase(
            crashReportStore = OkioCrashReportStore(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = JvmAppDispatchers(),
                appDataDirectory = appData.toString()
            ),
            systemEnvironment = JvmSystemEnvironment(),
            clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T10:15:30Z"))
        )

        val crash = recordCrash(error = IllegalStateException("Layout failed"), threadName = "AWT-EventQueue-0")
        val file = appData.resolve(OkioCrashReportStore.FOLDER_NAME).listDirectoryEntries().single()

        assertEquals(
            expected = Triple("crash-2026-10-26T10-15-30Z.txt", file.toString(), crash.text),
            actual = Triple(file.name, crash.path, file.readText())
        )
        assertTrue(actual = crash.text.contains(other = "java.lang.IllegalStateException: Layout failed"))
    }

    companion object {
        private const val TEMP_PREFIX = "notekea-crash"
    }
}

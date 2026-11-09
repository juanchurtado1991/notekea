package app.notekea.core.logging.data

import app.notekea.core.domain.FakeAppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class OkioLogStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fileSystem = FakeFileSystem().apply { createDirectories(dir = LOGS) }

    @Test
    fun `the recent lines come from the oldest file to the current one, the last ones kept`() = runTest(
        testDispatcher
    ) {
        write(name = "notekea.2.log", text = "a1\na2\n")
        write(name = "notekea.1.log", text = "b1\n\nb2\n")
        write(name = "notekea.log", text = "c1\nc2\n")

        assertEquals(expected = listOf("a2", "b1", "b2", "c1", "c2"), actual = store().recentLines(count = 5))
    }

    @Test
    fun `no log folder reads as no lines`() = runTest(testDispatcher) {
        assertEquals(
            expected = emptyList(),
            actual = OkioLogStore(
                fileSystem = FakeFileSystem(),
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
                appDataDirectory = APP_DATA
            ).recentLines(count = 10)
        )
    }

    private fun store() = OkioLogStore(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
        appDataDirectory = APP_DATA
    )

    private fun write(
        name: String,
        text: String
    ) {
        fileSystem.write(file = LOGS / name) { writeUtf8(string = text) }
    }

    private companion object {
        const val APP_DATA = "/home/ana/.local/share/Notekea"
        val LOGS = APP_DATA.toPath() / LogFiles.FOLDER
    }
}

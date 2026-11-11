package app.notekea.sync.data

import app.notekea.core.domain.FakeAppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/** A vault's size: its files in every folder, its Git history not, as GitHub's 1 GB is warned of from it. */
class OkioVaultSizesTest {
    private val dispatcher = StandardTestDispatcher()
    private val fileSystem = FakeFileSystem()

    @Test
    fun `every file of every folder counts, the git folder does not`() = runTest(dispatcher) {
        val vault = "/home/ana/notes".toPath()
        fileSystem.createDirectories(dir = vault / "Videos" / "Trips")
        fileSystem.createDirectories(dir = vault / ".git" / "objects")
        fileSystem.write(file = vault / "Welcome.md") { writeUtf8(string = "12345") }

        fileSystem
            .write(file = vault / "Videos" / "Trips" / "Lisbon.mp4") {
                write(ByteArray(size = 100))
            }

        fileSystem
            .write(file = vault / ".git" / "objects" / "pack") { write(ByteArray(size = 1_000)) }

        val sizes = OkioVaultSizes(
            fileSystem = fileSystem,
            dispatchers = FakeAppDispatchers(dispatcher = dispatcher)
        )

        assertEquals(
            expected = 105L,
            actual = sizes.bytesOf(vaultPath = vault.toString())
        )
    }
}

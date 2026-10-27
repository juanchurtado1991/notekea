package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.NoteTimes
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** When notes were created and modified, read from real files for the side panel's Note info. */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class NoteTimesOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val vault = createTempDirectory(prefix = TEMP_PREFIX)
    private val reader = OkioNoteTimesReader(
        fileSystem = FileSystem.SYSTEM,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @AfterTest
    fun tearDown() {
        vault.deleteRecursively()
    }

    @Test
    fun `a note's modified time is read from its file, and a missing note has no times`() = runTest(testDispatcher) {
        val note = vault.resolve("Recipes").createDirectories().resolve("Café con leche.md")
        note.writeText(text = "# Café\n")
        Files.setLastModifiedTime(note, FileTime.fromMillis(MODIFIED.toEpochMilliseconds()))

        assertEquals(
            expected = MODIFIED to NoteTimes(createdAt = null, modifiedAt = null),
            actual = reader.times(vaultPath = vault.toString(), path = RelativePath(value = NOTE)).modifiedAt to
                reader.times(vaultPath = vault.toString(), path = RelativePath(value = "Gone.md"))
        )
    }

    companion object {
        private const val NOTE = "Recipes/Café con leche.md"
        private const val TEMP_PREFIX = "notekea-times"
        private val MODIFIED = Instant.parse(input = "2026-10-20T18:05:00Z")
    }
}

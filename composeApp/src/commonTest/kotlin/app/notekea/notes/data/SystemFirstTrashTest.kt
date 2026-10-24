package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FakeTrash
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TrashLocation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemFirstTrashTest {
    private val testDispatcher = StandardTestDispatcher()
    private val vaultTrash = FakeTrash()
    private val systemTrashRequests = mutableListOf<String>()

    @Test
    fun `the system trash is used when it takes the entry`() = runTest(context = testDispatcher) {
        val outcome = trash(systemTrashAccepts = true).moveToTrash(vaultPath = VAULT, path = NOTE)

        assertEquals(
            expected = Outcome.Success(value = TrashLocation.SYSTEM_TRASH),
            actual = outcome
        )
        assertEquals(
            expected = listOf(listOf("$VAULT/${NOTE.value}"), emptyList()),
            actual = listOf(systemTrashRequests, vaultTrash.trashed.map { it.value })
        )
    }

    @Test
    fun `the vault trash folder takes over when the system trash refuses`() = runTest(context = testDispatcher) {
        vaultTrash.outcome = Outcome.Failure(error = NotesError.EntryMissing(path = NOTE.value))

        val outcome = trash(systemTrashAccepts = false).moveToTrash(vaultPath = VAULT, path = NOTE)

        assertEquals(
            expected = Outcome.Failure(error = NotesError.EntryMissing(path = NOTE.value)),
            actual = outcome
        )
        assertEquals(
            expected = listOf(NOTE),
            actual = vaultTrash.trashed
        )
    }

    private fun trash(
        systemTrashAccepts: Boolean
    ): SystemFirstTrash = SystemFirstTrash(
        moveToSystemTrash = { path ->
            systemTrashRequests += path
            systemTrashAccepts
        },
        vaultTrash = vaultTrash,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    companion object {
        private const val VAULT = "/home/ana/Notes"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}

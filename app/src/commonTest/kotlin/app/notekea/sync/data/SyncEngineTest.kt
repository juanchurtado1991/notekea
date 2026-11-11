package app.notekea.sync.data

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.FakeGitRepository
import app.notekea.sync.domain.FakeSyncSettingsStore
import app.notekea.sync.domain.FakeTokenStore
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** A vault's sync over a fake repository: when it commits, pulls and pushes, and what the status bar is told. */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngineTest {
    private val repository = FakeGitRepository()
    private val tokens = FakeTokenStore()
    private val settings = FakeSyncSettingsStore()
    private val edits = MutableSharedFlow<Unit>()

    private fun TestScope.engine(): SyncEngine = SyncEngine(
        vaultPath = VAULT,
        repository = repository,
        tokens = tokens,
        settingsStore = settings,
        edits = { edits },
        scope = backgroundScope
    )

    private fun TestScope.settle() {
        advanceTimeBy(delayTime = 100.milliseconds)
        runCurrent()
    }

    @Test
    fun `a vault that does not sync stays quiet`() = runTest {
        val engine = engine()
        engine.start()
        settle()

        assertEquals(
            expected = listOf<Any?>(SyncStatus.NotConfigured, emptyList<String>()),
            actual = listOf(engine.status.value, repository.calls)
        )
    }

    @Test
    fun `connecting keeps the token in the keychain, and an edit is committed, pulled and pushed after 10 s`() =
        runTest {
            val engine = engine()

            engine.connect(
                settings = SyncSettings(remoteUrl = URL),
                token = TOKEN
            )

            settle()
            repository.calls.clear()

            edits.emit(value = Unit)
            advanceTimeBy(delayTime = 9.seconds)
            runCurrent()
            val early = repository.calls.toList()
            advanceTimeBy(delayTime = 2.seconds)
            runCurrent()
            settle()

            assertEquals(
                expected = listOf<Any?>(TOKEN, emptyList<String>(), listOf("commit", "pull $TOKEN", "push"), UP_TO_DATE),
                actual = listOf(tokens.tokens[URL], early, repository.calls, engine.status.value)
            )
        }

    @Test
    fun `with auto-sync off an edit is only counted, and Sync now sends it`() = runTest {
        settings.settings[VAULT] = SyncSettings(
            remoteUrl = URL,
            isAuto = false
        )

        val engine = engine()
        engine.start()
        settle()
        repository.calls.clear()
        repository.pending = 2

        edits.emit(value = Unit)
        advanceTimeBy(delayTime = 11.seconds)
        settle()
        val counted = engine.status.value to repository.calls.toList()

        engine.syncNow()

        assertEquals(
            expected = (SyncStatus.Pending(changes = 2) to emptyList<String>()) to UP_TO_DATE,
            actual = counted to engine.status.value
        )
    }

    @Test
    fun `offline is shown and tried again on the next interval, a refused token is shown`() =
        runTest {
            settings.settings[VAULT] = SyncSettings(
                remoteUrl = URL,
                intervalMinutes = 1
            )

            repository.pullError = SyncError.Offline
            val engine = engine()
            engine.start()
            settle()
            val offline = engine.status.value

            repository.pullError = null
            advanceTimeBy(delayTime = 1.minutes)
            settle()
            val back = engine.status.value

            repository.pullError = SyncError.AuthFailed
            engine.syncNow()

            assertEquals(
                expected = listOf(SyncStatus.Offline, UP_TO_DATE, SyncStatus.AuthFailed),
                actual = listOf(offline, back, engine.status.value)
            )
        }

    @Test
    fun `a conflict stays in the status until it is settled`() = runTest {
        settings.settings[VAULT] = SyncSettings(remoteUrl = URL)
        repository.conflictsAfterPull = listOf(PLAN)
        val engine = engine()
        engine.start()
        settle()
        val conflict = engine.status.value

        engine.resolve(
            note = PLAN,
            choice = ConflictChoice.BOTH
        )

        assertEquals(
            expected = listOf<Any?>(SyncStatus.Conflict(notes = listOf(PLAN)), UP_TO_DATE, true),
            actual = listOf(
                conflict,
                engine.status.value,
                "resolve ${PLAN.value} BOTH" in repository.calls
            )
        )
    }

    @Test
    fun `a push refused because the remote moved is pulled and pushed again`() = runTest {
        settings.settings[VAULT] = SyncSettings(remoteUrl = URL)
        repository.pushErrors += SyncError.Failed(message = "rejected")
        val engine = engine()
        engine.syncNow()

        assertEquals(
            expected = listOf<Any?>(
                listOf("commit", "pull null", "push", "pull null", "push"),
                UP_TO_DATE
            ),
            actual = listOf(repository.calls, engine.status.value)
        )
    }

    @Test
    fun `disconnecting forgets the settings and the token`() = runTest {
        val engine = engine()

        engine.connect(
            settings = SyncSettings(remoteUrl = URL),
            token = TOKEN
        )

        engine.disconnect()

        assertEquals(
            expected = listOf<Any?>(null, null, SyncStatus.NotConfigured),
            actual = listOf(settings.settings[VAULT], tokens.tokens[URL], engine.status.value)
        )
    }

    private companion object {
        const val VAULT = "/vaults/Notes"
        const val URL = "https://example.com/notes.git"
        const val TOKEN = "secret"
        val PLAN = RelativePath(value = "Plan.md")
        val UP_TO_DATE = SyncStatus.UpToDate
    }
}

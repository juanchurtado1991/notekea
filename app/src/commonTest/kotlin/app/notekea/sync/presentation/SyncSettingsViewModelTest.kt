package app.notekea.sync.presentation

import app.notekea.sync.data.SyncEngine
import app.notekea.sync.domain.FakeGitRepository
import app.notekea.sync.domain.FakeSyncSettingsStore
import app.notekea.sync.domain.FakeTokenStore
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.domain.VaultSyncs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** Settings → Sync: what address it takes, what connecting keeps, and Stop syncing. */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncSettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeGitRepository()
    private val tokens = FakeTokenStore()
    private val settings = FakeSyncSettingsStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(vaultPath: String? = VAULT): SyncSettingsViewModel {
        val engine = SyncEngine(
            vaultPath = VAULT,
            repository = repository,
            tokens = tokens,
            settingsStore = settings,
            edits = { emptyFlow() },
            scope = backgroundScope
        )

        return SyncSettingsViewModel(
            engines = VaultSyncs { engine },
            settingsStore = settings,
            tokens = tokens,
            vaultPath = vaultPath
        )
    }

    private fun TestScope.connect(
        viewModel: SyncSettingsViewModel,
        url: String,
        token: String = ""
    ): SyncSettingsState {
        viewModel.onIntent(intent = SyncSettingsIntent.OnUrlChange(url = url))
        viewModel.onIntent(intent = SyncSettingsIntent.OnTokenChange(token = token))
        viewModel.onIntent(intent = SyncSettingsIntent.OnConnect)
        advanceTimeBy(delayTime = 1.seconds)
        runCurrent()

        return viewModel.state.value
    }

    @Test
    fun `an https or a local address connects, anything else is refused before trying`() =
        runTest(dispatcher) {
            val refused = connect(
                viewModel = viewModel(),
                url = "git@github.com:ana/notes.git"
            )

            val https = connect(
                viewModel = viewModel(),
                url = "https://example.com/notes.git",
                token = "secret"
            )

            val local = connect(
                viewModel = viewModel(),
                url = "file:/home/ana/remote.git"
            )

            assertEquals(
                expected = listOf<Any?>(
                    SyncError.BadAddress,
                    false,
                    null,
                    true,
                    "secret",
                    "",
                    null,
                    true
                ),
                actual = listOf(
                    refused.error,
                    refused.isConnected,
                    https.error,
                    https.isConnected,
                    tokens.tokens["https://example.com/notes.git"],
                    https.token,
                    local.error,
                    local.isConnected
                )
            )
        }

    @Test
    fun `a refused token is told, and Stop syncing forgets the vault's settings`() =
        runTest(dispatcher) {
            repository.connectError = SyncError.AuthFailed
            val viewModel = viewModel()

            val refused = connect(
                viewModel = viewModel,
                url = URL,
                token = "wrong"
            )

            repository.connectError = null

            connect(
                viewModel = viewModel,
                url = URL,
                token = "right"
            )

            viewModel.onIntent(intent = SyncSettingsIntent.OnDisconnect)
            advanceTimeBy(delayTime = 1.seconds)
            runCurrent()

            assertEquals(
                expected = listOf<Any?>(
                    SyncError.AuthFailed,
                    SyncStatus.NotConfigured,
                    null,
                    false
                ),
                actual = listOf(
                    refused.error,
                    viewModel.state.value.status,
                    settings.settings[VAULT],
                    viewModel.state.value.isConnected
                )
            )
        }

    @Test
    fun `without a vault there is nothing to sync, and a synced vault shows its settings`() =
        runTest(dispatcher) {
            settings.settings[VAULT] = SyncSettings(
                remoteUrl = URL,
                isAuto = false,
                intervalMinutes = 15
            )

            tokens.tokens[URL] = "secret"
            val none = viewModel(vaultPath = null)
            val synced = viewModel()
            runCurrent()

            assertEquals(
                expected = listOf<Any?>(false, URL, false, 15, true),
                actual = listOf(
                    none.state.value.hasVault,
                    synced.state.value.url,
                    synced.state.value.isAuto,
                    synced.state.value.intervalMinutes,
                    synced.state.value.hasToken
                )
            )
        }

    // Code review of 2026-11-11: an address typed but not connected was saved by a change of auto-sync, and the
    // token was then looked up under it.
    @Test
    fun `a change of auto-sync keeps the address it is connected to, not one only typed`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            connect(
                viewModel = viewModel,
                url = "https://example.com/notes.git",
                token = "secret"
            )

            viewModel.onIntent(intent = SyncSettingsIntent.OnUrlChange(url = "https://example.com/other.git"))
            viewModel.onIntent(intent = SyncSettingsIntent.OnAutoToggle)
            advanceTimeBy(delayTime = 1.seconds)
            runCurrent()

            assertEquals(
                expected = SyncSettings(
                    remoteUrl = "https://example.com/notes.git",
                    isAuto = false
                ),
                actual = settings.read(vaultPath = VAULT)
            )
        }

    private companion object {
        const val VAULT = "/vaults/Notes"
        const val URL = "https://example.com/notes.git"
    }
}

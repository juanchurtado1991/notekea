package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.Outcome
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.vault.domain.Vault
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.VaultFixture
import app.notekea.vault.domain.VaultImporter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Import a vault from a repository, on the welcome screen (the developer's request of 2026-11-14). */
@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeImportTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = VaultFixture()

    // What bringing a repository answers; a gate holds it open to cancel it half way.
    private var importFailure: VaultError? = null
    private var importGate: CompletableDeferred<Unit>? = null
    private val imported = mutableListOf<List<String?>>()

    private val importer = object : VaultImporter {
        override fun isAddress(remoteUrl: String): Boolean =
            remoteUrl.startsWith(prefix = "https://")

        override suspend fun import(
            vaultPath: String,
            remoteUrl: String,
            token: String?
        ): Outcome<Unit, VaultError> {
            imported += listOf(vaultPath, remoteUrl, token)
            importGate?.await()

            return importFailure?.let {
                Outcome.Failure(error = it)
            } ?: Outcome.Success(value = Unit)
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runWelcomeTest(block: suspend TestScope.() -> Unit) = runTest(
        context = testDispatcher
    ) { block() }

    private fun TestScope.startedViewModel(): WelcomeViewModel {
        val viewModel = WelcomeViewModel(
            platformConventions = WelcomePreviewData.platformConventions,
            createVault = fixture.createVault,
            getRecentVaults = fixture.getRecentVaults,
            locateVault = fixture.locateVault,
            openVault = fixture.openVault,
            folderPicker = fixture.folderPicker,
            removeRecentVault = fixture.removeRecentVault,
            restoreLastVault = fixture.restoreLastVault,
            vaultFileSystem = fixture.fileSystem,
            preferencesStore = FakeAppPreferencesStore(initial = AppPreferences()),
            sampleVault = { Outcome.Success(value = Unit) },
            importVault = fixture.importVault(importer = importer)
        )

        advanceUntilIdle()

        return viewModel
    }

    private fun TestScope.importing(
        viewModel: WelcomeViewModel,
        url: String = REPOSITORY,
        token: String = ""
    ) {
        viewModel.onIntent(intent = WelcomeIntent.OnImportClick)
        advanceUntilIdle()
        viewModel.onIntent(intent = WelcomeIntent.OnImportUrlChange(url = url))
        viewModel.onIntent(intent = WelcomeIntent.OnImportTokenChange(token = token))
        viewModel.onIntent(intent = WelcomeIntent.OnImportConfirm)
        advanceUntilIdle()
    }

    @Test
    fun `a repository is brought into a folder named after it, in Documents, and opens as the vault`() =
        runWelcomeTest {
            val viewModel = startedViewModel()

            importing(
                viewModel = viewModel,
                token = "secret"
            )

            val state = viewModel.state.value

            assertEquals(
                expected = listOf<Any?>(
                    Vault.fromPath(path = IMPORTED),
                    null,
                    listOf(listOf(IMPORTED, REPOSITORY, "secret")),
                    false
                ),
                actual = listOf<Any?>(state.openedVault, state.importForm, imported, state.isBusy)
            )
        }

    @Test
    fun `the folder follows the address until the person names it`() = runWelcomeTest {
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = WelcomeIntent.OnImportClick)
        advanceUntilIdle()
        viewModel.onIntent(intent = WelcomeIntent.OnImportUrlChange(url = REPOSITORY))
        val followed = viewModel.state.value.importForm?.name
        viewModel.onIntent(intent = WelcomeIntent.OnImportNameChange(name = "Work"))
        viewModel.onIntent(intent = WelcomeIntent.OnImportUrlChange(url = "https://example.com/other.git"))

        assertEquals(
            expected = "notes" to "Work",
            actual = followed to viewModel.state.value.importForm?.name
        )
    }

    @Test
    fun `each way an import fails keeps the panel open with its problem, and leaves no folder`() =
        runWelcomeTest {
            val problems = listOf(
                VaultError.RepositoryRefused,
                VaultError.RepositoryOffline,
                VaultError.RepositoryBadAddress,
                VaultError.RepositoryFailed(message = "not a repository")
            )

            val seen = problems.map { problem ->
                val viewModel = startedViewModel()
                importFailure = problem
                importing(viewModel = viewModel)
                val state = viewModel.state.value

                listOf(state.error, state.importForm != null, state.isBusy, state.openedVault)
            }

            assertEquals(
                expected = problems.map {
                    listOf(it, true, false, null)
                } to List(size = 4) { IMPORTED },
                actual = seen to fixture.fileSystem.discarded
            )
        }

    @Test
    fun `no address, a name no folder can have and a folder that holds files are said without asking Git`() =
        runWelcomeTest {
            fixture.fileSystem.filled += IMPORTED

            val noAddress = startedViewModel().also {
                importing(
                    viewModel = it,
                    url = "  "
                )
            }

            val taken = startedViewModel().also { importing(viewModel = it) }

            val badName = startedViewModel().also {
                it.onIntent(intent = WelcomeIntent.OnImportClick)
                advanceUntilIdle()
                it.onIntent(intent = WelcomeIntent.OnImportUrlChange(url = REPOSITORY))
                it.onIntent(intent = WelcomeIntent.OnImportNameChange(name = "a/b"))
                it.onIntent(intent = WelcomeIntent.OnImportConfirm)
                advanceUntilIdle()
            }

            assertEquals(
                expected = listOf<Any?>(
                    VaultError.RepositoryBadAddress,
                    VaultError.FolderAlreadyExists(path = IMPORTED),
                    VaultError.InvalidName,
                    emptyList<Any>()
                ),
                actual = listOf<Any?>(
                    noAddress.state.value.error,
                    taken.state.value.error,
                    badName.state.value.error,
                    imported
                )
            )
        }

    @Test
    fun `Cancel stops an import half way, takes its folder away and leaves the panel to try again`() =
        runWelcomeTest {
            val viewModel = startedViewModel()
            importGate = CompletableDeferred()
            importing(viewModel = viewModel)
            val busy = viewModel.state.value.isBusy

            viewModel.onIntent(intent = WelcomeIntent.OnImportCancel)
            advanceUntilIdle()
            val stopped = viewModel.state.value

            viewModel.onIntent(intent = WelcomeIntent.OnImportCancel)
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(true, false, true, null, listOf(IMPORTED), null),
                actual = listOf<Any?>(
                    busy,
                    stopped.isBusy,
                    stopped.importForm != null,
                    stopped.openedVault,
                    fixture.fileSystem.discarded,
                    viewModel.state.value.importForm
                )
            )
        }

    @Test
    fun `the import's folder can be put elsewhere, and editing a field clears its problem`() =
        runWelcomeTest {
            val viewModel = startedViewModel()
            importFailure = VaultError.RepositoryRefused
            importing(viewModel = viewModel)
            val failed = viewModel.state.value.error

            fixture.folderPicker.nextPick = "/home/ana/Work"
            viewModel.onIntent(intent = WelcomeIntent.OnImportLocationClick)
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(VaultError.RepositoryRefused, null, "/home/ana/Work"),
                actual = listOf<Any?>(
                    failed,
                    viewModel.state.value.error,
                    viewModel.state.value.importForm?.parentPath
                )
            )
        }

    /** For the welcome screen's other tests, which import nothing. */
    object NoImporter : VaultImporter {
        override fun isAddress(remoteUrl: String): Boolean = false

        override suspend fun import(
            vaultPath: String,
            remoteUrl: String,
            token: String?
        ): Outcome<Unit, VaultError> = Outcome.Failure(error = VaultError.RepositoryBadAddress)
    }

    private companion object {
        const val REPOSITORY = "https://github.com/ana/notes.git"
        const val IMPORTED = "/home/ana/Documents/notes"
    }
}

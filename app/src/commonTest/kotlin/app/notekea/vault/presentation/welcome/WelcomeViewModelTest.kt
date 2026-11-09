package app.notekea.vault.presentation.welcome

import app.notekea.vault.domain.FakeVaultFileSystem
import app.notekea.vault.domain.FolderAccess
import app.notekea.vault.domain.Vault
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.VaultFixture
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = VaultFixture()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startup reopens the last vault`() = runWelcomeTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.NOTES))

        val state = startedViewModel().state.value

        assertEquals(
            expected = false,
            actual = state.isLoading
        )
        assertEquals(
            expected = Vault.fromPath(path = VaultFixture.NOTES),
            actual = state.openedVault
        )
    }

    @Test
    fun `without recents the welcome screen shows only its actions`() = runWelcomeTest {
        val state = startedViewModel().state.value

        assertEquals(
            expected = WelcomeState(platformConventions = WelcomePreviewData.platformConventions, isLoading = false),
            actual = state
        )
    }

    @Test
    fun `a missing last vault stays in the list instead of showing an error`() = runWelcomeTest {
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.ARCHIVE))

        val state = startedViewModel().state.value

        assertNull(actual = state.openedVault)
        assertNull(actual = state.error)
        assertTrue(actual = state.recentVaults.single().isMissing)
    }

    @Test
    fun `a picked folder opens and joins the recent list`() = runWelcomeTest {
        fixture.addFolder(path = VaultFixture.WORK)
        fixture.folderPicker.nextPick = VaultFixture.WORK
        val viewModel = startedViewModel()

        send(viewModel = viewModel, intent = WelcomeIntent.OnOpenFolderClick)

        assertEquals(
            expected = VaultFixture.WORK,
            actual = viewModel.state.value.openedVault
                ?.path
        )
        assertEquals(
            expected = listOf(VaultFixture.WORK),
            actual = viewModel.state.value.recentVaults
                .map { it.vault.path }
        )
    }

    @Test
    fun `cancelling the picker changes nothing`() = runWelcomeTest {
        val viewModel = startedViewModel()
        val before = viewModel.state.value

        send(viewModel = viewModel, intent = WelcomeIntent.OnOpenFolderClick)

        assertEquals(
            expected = before,
            actual = viewModel.state.value
        )
    }

    @Test
    fun `a permission error is shown until dismissed`() = runWelcomeTest {
        fixture.addFolder(path = VaultFixture.NOTES, access = FolderAccess.NOT_READABLE)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.WORK))
        fixture.addFolder(path = VaultFixture.WORK)
        val viewModel = startedViewModel()
        send(viewModel = viewModel, intent = WelcomeIntent.OnSwitchVaultClick)

        send(viewModel = viewModel, intent = WelcomeIntent.OnRecentVaultClick(path = VaultFixture.NOTES))
        val failed = viewModel.state.value
        send(viewModel = viewModel, intent = WelcomeIntent.OnErrorDismiss)

        assertEquals(
            expected = VaultError.PermissionDenied(path = VaultFixture.NOTES),
            actual = failed.error
        )
        assertEquals(
            expected = false,
            actual = failed.isBusy
        )
        assertNull(actual = viewModel.state.value.error)
    }

    @Test
    fun `creating a vault suggests a name and location, then opens it`() = runWelcomeTest {
        val viewModel = startedViewModel()

        send(viewModel = viewModel, intent = WelcomeIntent.OnCreateVaultClick(suggestedName = SUGGESTED_NAME))
        val suggested = viewModel.state.value.newVaultForm
        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultNameChange(name = NAME))
        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultConfirm)

        assertEquals(
            expected = NewVaultForm(name = SUGGESTED_NAME, parentPath = FakeVaultFileSystem.DEFAULT_PARENT),
            actual = suggested
        )
        assertEquals(
            expected = FakeVaultFileSystem.DEFAULT_PARENT + FakeVaultFileSystem.SEPARATOR + NAME,
            actual = viewModel.state.value.openedVault
                ?.path
        )
        assertNull(actual = viewModel.state.value.newVaultForm)
    }

    @Test
    fun `the new vault location can be changed with the picker`() = runWelcomeTest {
        fixture.folderPicker.nextPick = VaultFixture.WORK
        val viewModel = startedViewModel()
        send(viewModel = viewModel, intent = WelcomeIntent.OnCreateVaultClick(suggestedName = SUGGESTED_NAME))

        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultLocationClick)

        assertEquals(
            expected = VaultFixture.WORK,
            actual = viewModel.state.value.newVaultForm
                ?.parentPath
        )
        assertEquals(
            expected = listOf<String?>(FakeVaultFileSystem.DEFAULT_PARENT),
            actual = fixture.folderPicker.initialPaths
        )
    }

    @Test
    fun `an invalid name keeps the form open with an error, and cancel closes it`() = runWelcomeTest {
        val viewModel = startedViewModel()
        send(viewModel = viewModel, intent = WelcomeIntent.OnCreateVaultClick(suggestedName = INVALID_NAME))

        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultConfirm)
        val failed = viewModel.state.value
        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultCancel)

        assertEquals(
            expected = VaultError.InvalidName,
            actual = failed.error
        )
        assertEquals(
            expected = INVALID_NAME,
            actual = failed.newVaultForm?.name
        )
        assertNull(actual = viewModel.state.value.newVaultForm)
    }

    @Test
    fun `editing the name clears its error`() = runWelcomeTest {
        val viewModel = startedViewModel()
        send(viewModel = viewModel, intent = WelcomeIntent.OnCreateVaultClick(suggestedName = INVALID_NAME))
        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultConfirm)

        send(viewModel = viewModel, intent = WelcomeIntent.OnNewVaultNameChange(name = NAME))

        assertNull(actual = viewModel.state.value.error)
    }

    @Test
    fun `form intents without an open form are ignored`() = runWelcomeTest {
        val viewModel = startedViewModel()
        val before = viewModel.state.value

        listOf(
            WelcomeIntent.OnNewVaultConfirm,
            WelcomeIntent.OnNewVaultLocationClick,
            WelcomeIntent.OnNewVaultNameChange(name = NAME)
        ).forEach { send(viewModel = viewModel, intent = it) }

        assertEquals(
            expected = before,
            actual = viewModel.state.value
        )
    }

    @Test
    fun `a missing vault can be located or removed`() = runWelcomeTest {
        fixture.store.recentVaults =
            listOf(fixture.recent(path = VaultFixture.ARCHIVE), fixture.recent(path = VaultFixture.NOTES))
        fixture.addFolder(path = VaultFixture.WORK)
        fixture.folderPicker.nextPick = VaultFixture.WORK
        val viewModel = startedViewModel()

        send(viewModel = viewModel, intent = WelcomeIntent.OnRemoveRecentVaultClick(path = VaultFixture.NOTES))
        send(viewModel = viewModel, intent = WelcomeIntent.OnLocateVaultClick(missingPath = VaultFixture.ARCHIVE))

        assertEquals(
            expected = listOf(VaultFixture.WORK),
            actual = viewModel.state.value.recentVaults
                .map { it.vault.path }
        )
        assertEquals(
            expected = VaultFixture.WORK,
            actual = viewModel.state.value.openedVault
                ?.path
        )
    }

    @Test
    fun `switching vault returns to the welcome screen with fresh recents`() = runWelcomeTest {
        fixture.addFolder(path = VaultFixture.NOTES)
        fixture.store.recentVaults = listOf(fixture.recent(path = VaultFixture.NOTES))
        val viewModel = startedViewModel()

        send(viewModel = viewModel, intent = WelcomeIntent.OnSwitchVaultClick)

        assertNull(actual = viewModel.state.value.openedVault)
        assertEquals(
            expected = listOf(VaultFixture.NOTES),
            actual = viewModel.state.value.recentVaults
                .map { it.vault.path }
        )
    }

    @Test
    fun `an unexpected failure shows the generic error and can be dismissed`() = runWelcomeTest {
        fixture.folderPicker.failure = IllegalStateException(FAILURE)
        val viewModel = startedViewModel()

        send(viewModel = viewModel, intent = WelcomeIntent.OnOpenFolderClick)
        val failed = viewModel.state.value
        send(viewModel = viewModel, intent = WelcomeIntent.OnErrorDismiss)

        assertTrue(actual = failed.hasUnexpectedError)
        assertEquals(
            expected = false,
            actual = viewModel.state.value.hasUnexpectedError
        )
    }

    private fun runWelcomeTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    private fun TestScope.send(
        viewModel: WelcomeViewModel,
        intent: WelcomeIntent
    ) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
    }

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
            vaultFileSystem = fixture.fileSystem
        )
        advanceUntilIdle()
        return viewModel
    }

    companion object {
        private const val FAILURE = "Dialog crashed"
        private const val INVALID_NAME = "a/b"
        private const val NAME = "Journal"
        private const val SUGGESTED_NAME = "My Notes"
    }
}

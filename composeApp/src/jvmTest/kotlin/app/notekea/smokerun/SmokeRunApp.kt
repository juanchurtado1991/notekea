package app.notekea.smokerun

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.notekea.App
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.di.desktopModules
import app.notekea.notes.data.VaultFolderTrash
import app.notekea.notes.domain.AttachmentPicker
import app.notekea.notes.domain.FakeAttachmentPicker
import app.notekea.notes.domain.FakeFileOpener
import app.notekea.notes.domain.FakeFileRevealer
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.FileRevealer
import app.notekea.notes.domain.Trash
import app.notekea.notes.presentation.tree.VaultScope
import app.notekea.notes.presentation.tree.VaultTreeScreen
import app.notekea.vault.domain.FakeFolderPicker
import app.notekea.vault.domain.FolderPicker
import app.notekea.vault.presentation.welcome.WelcomeScreen
import okio.FileSystem
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * The real app for an automated smoke run (`ROADMAP.md` 1.16): every Koin module of the desktop app, with its data
 * (settings, database, crash reports) in a temporary folder instead of the user's, copies of the smoke vaults named
 * [vaults] (and what [prepare] writes into their folder), and the system's dialogs and apps replaced: the folder picker answers [folderPicker], files "open" in
 * [fileOpener] and [fileRevealer], and deleted entries go to the vault's `.trash` folder, never the system trash.
 * [Content] is the window's content, keyboard shortcuts included.
 */
@OptIn(ExperimentalPathApi::class)
internal class SmokeRunApp(
    vaults: List<String>,
    prepare: (vaults: File) -> Unit = {}
) : AutoCloseable {
    private val root: Path = Files.createTempDirectory(TEMP_PREFIX)
    val folderPicker = FakeFolderPicker()
    val attachmentPicker = FakeAttachmentPicker()
    val fileOpener = FakeFileOpener()
    val fileRevealer = FakeFileRevealer()

    // The window itself: in the app a window gets every key, here a focused node does, so once nothing else holds the
    // focus (the last tab closed) a run gives it back to the window for its shortcuts.
    val windowFocus = FocusRequester()

    // The app's view models live here, so closing the run stops their work (the vault's scan) with them.
    private val viewModels = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    init {
        // As the installed app finds it: its data folder exists before the database and the settings open there.
        Files.createDirectories(root.resolve(APP_DATA))
        vaults.forEach { name ->
            File(SMOKE_VAULTS, name).copyRecursively(target = root.resolve(VAULTS).resolve(name).toFile())
        }
        prepare(root.resolve(VAULTS).toFile())
        startKoin { modules(desktopModules + overrides()) }
    }

    /** Where the copy of the vault [name] is. */
    fun vaultPath(
        name: String
    ): String = root.resolve(VAULTS).resolve(name).toString()

    @Composable
    fun Content() {
        val dispatcher = remember { ShortcutDispatcher() }
        CompositionLocalProvider(
            LocalShortcutDispatcher provides dispatcher,
            LocalViewModelStoreOwner provides viewModels
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent(onPreviewKeyEvent = dispatcher::dispatch)
                    .focusRequester(focusRequester = windowFocus)
                    .focusable()
            ) {
                App {
                    WelcomeScreen(
                        onVaultNameChange = {},
                        openedVaultContent = { vault, onSwitchVault ->
                            VaultScope(vaultPath = vault.path) { close ->
                                VaultTreeScreen(
                                    vaultPath = vault.path,
                                    vaultName = vault.name,
                                    onSwitchVault = { close(onSwitchVault) }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Stops the app. Its folder is deleted when the test process ends, not now: work the app started in the background
     * (the note cache's database) may still be finishing, and would fail on a folder already gone.
     */
    override fun close() {
        viewModels.viewModelStore.clear()
        stopKoin()
        Runtime.getRuntime().addShutdownHook(Thread { root.deleteRecursively() })
    }

    private fun overrides() = module {
        single<PlatformConventions> {
            get<ResolvePlatformConventionsUseCase>().invoke().copy(
                appDataDirectory = root.resolve(APP_DATA).toString()
            )
        }
        single<FolderPicker> { folderPicker }
        single<AttachmentPicker> { attachmentPicker }
        single<FileOpener> { fileOpener }
        single<FileRevealer> { fileRevealer }
        single<Trash> { VaultFolderTrash(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    }

    companion object {
        private const val APP_DATA = "app-data"
        private const val SMOKE_VAULTS = "../smoke-data/vaults"
        private const val TEMP_PREFIX = "notekea-smoke-run"
        private const val VAULTS = "vaults"
    }
}

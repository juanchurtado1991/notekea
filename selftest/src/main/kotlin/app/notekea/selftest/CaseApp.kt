package app.notekea.selftest

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.notekea.App
import app.notekea.core.appearance.domain.GetUiScaleUseCase
import app.notekea.core.appearance.domain.SaveUiScaleUseCase
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.LocalWindowCloseGuard
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.core.presentation.platform.UiZoom
import app.notekea.core.presentation.platform.WindowCloseGuard
import app.notekea.di.desktopModules
import app.notekea.notes.data.VaultFolderTrash
import app.notekea.notes.domain.AttachmentPicker
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.FileRevealer
import app.notekea.notes.domain.Trash
import app.notekea.notes.presentation.tree.VaultScope
import app.notekea.notes.presentation.tree.VaultTreeScreen
import app.notekea.vault.domain.FolderPicker
import app.notekea.vault.presentation.welcome.WelcomeScreen
import okio.FileSystem
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Path

/**
 * The real Notekea for one automated case or smoke run: every Koin module of the desktop app, with its data
 * (settings, database, crash reports) in [appData] instead of the user's folder, its vaults in [vaults], a home folder
 * of its own for new vaults ([home]), and the system's dialogs and apps replaced: the pickers answer what the case
 * set, files "open" in [fileOpener] and [fileRevealer], web links in [uriHandler], and deleted entries go to the
 * vault's `.trash` folder, never the system trash. [Content] is the window's content, keyboard shortcuts included;
 * [closeGuard] is what the window runs before it closes (the editor saving the note being typed).
 */
class CaseApp(
    private val appData: Path,
    val vaults: Path
) : AutoCloseable {
    val home: Path = appData.resolveSibling(HOME)
    val uriHandler = RecordingUriHandler()
    val closeGuard = WindowCloseGuard()
    private var isClosed = false

    val folderPicker = ScriptedFolderPicker()
    val attachmentPicker = ScriptedAttachmentPicker()
    val fileOpener = RecordingFileOpener()
    val fileRevealer = RecordingFileRevealer()

    // The window itself: in the app a window gets every key, here a focused node does, so once nothing else holds the
    // focus (the last tab closed) a case gives it back to the window for its shortcuts.
    val windowFocus = FocusRequester()

    // The app's view models live here, so closing the case stops their work (the vault's scan) with them.
    private val viewModels = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    private val koin: Koin

    init {
        // As the installed app finds it: its data folder exists before the database and the settings open there.
        Files.createDirectories(appData)
        Files.createDirectories(home.resolve(DOCUMENTS))
        koin = startKoin { modules(desktopModules + overrides()) }.koin
    }

    /** Where the vault [name] is. */
    fun vaultPath(name: String): String = vaults.resolve(name).toString()

    @Composable
    fun Content() {
        val dispatcher = remember { ShortcutDispatcher() }
        CompositionLocalProvider(
            LocalShortcutDispatcher provides dispatcher,
            LocalViewModelStoreOwner provides viewModels,
            LocalWindowCloseGuard provides closeGuard,
            LocalUriHandler provides uriHandler
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onPreviewKeyEvent(onPreviewKeyEvent = dispatcher::dispatch)
                    .focusRequester(focusRequester = windowFocus)
                    .focusable()
            ) {
                // The window's zoom, as the app's main window has it (1.14-14).
                UiZoom(
                    platformConventions = koin.get(),
                    loadScale = { koin.get<GetUiScaleUseCase>()() },
                    saveScale = { koin.get<SaveUiScaleUseCase>()(scale = it) }
                ) { Window() }
            }
        }
    }

    @Composable
    private fun Window() {
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

    /** Stops the app once: the view models' work, then Koin. */
    override fun close() {
        if (isClosed) return
        isClosed = true
        viewModels.viewModelStore.clear()
        stopKoin()
    }

    private fun overrides() = module {
        single<PlatformConventions> {
            get<ResolvePlatformConventionsUseCase>().invoke().copy(
                appDataDirectory = appData.toString(),
                homeDirectory = home.toString()
            )
        }
        single<FolderPicker> { folderPicker }
        single<AttachmentPicker> { attachmentPicker }
        single<FileOpener> { fileOpener }
        single<FileRevealer> { fileRevealer }
        single<Trash> { VaultFolderTrash(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    }

    private companion object {
        const val HOME = "home"
        const val DOCUMENTS = "Documents"
    }
}

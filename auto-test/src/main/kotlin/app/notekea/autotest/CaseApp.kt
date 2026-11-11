package app.notekea.autotest

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
import app.notekea.NotekeaAppLocals
import app.notekea.NotekeaRoot
import app.notekea.ai.data.ModelFolder
import app.notekea.ai.domain.ActiveModelStore
import app.notekea.clips.data.KtorPageFetcher
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.logging.domain.TextClipboard
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.presentation.platform.AppEnvironment
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.core.presentation.platform.WindowCloseGuard
import app.notekea.core.presentation.platform.WindowHost
import app.notekea.di.desktopModules
import app.notekea.editor.domain.AttachmentPicker
import app.notekea.sync.domain.VaultSync
import app.notekea.sync.domain.VaultSyncs
import app.notekea.tree.data.VaultFolderTrash
import app.notekea.tree.domain.FileRevealer
import app.notekea.tree.domain.Trash
import app.notekea.vault.domain.FolderPicker
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Path

/**
 * The real Notekea for one case: the desktop app's Koin modules, its data in [appData], its vaults in [vaults], a home
 * of its own ([home]), the system's dialogs and apps replaced ([fileOpener], [uriHandler], [clipboard]…) and the web by
 * a documentation site ([StandInDocsSite]). [closeGuard] runs before the window closes.
 */
class CaseApp(
    private val appData: Path,
    val vaults: Path
) : AutoCloseable {
    val home: Path = appData.resolveSibling(HOME)
    val uriHandler = RecordingUriHandler()
    val clipboard = RecordingTextClipboard()
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
    private val host: WindowHost

    init {
        // As the installed app finds it: its data folder exists before the database and the settings open there.
        Files.createDirectories(appData)
        Files.createDirectories(home.resolve(DOCUMENTS))

        koin = startKoin { modules(desktopModules + overrides()) }.koin

        host = WindowHost(conventions = koin.get())
    }

    val closeGuard: WindowCloseGuard get() = host.closeGuard
    val shortcuts: ShortcutDispatcher get() = host.shortcuts

    /** The title the app's window shows now. */
    val windowTitle: String get() = host.title

    /**
     * Puts the chat model of [ChatModels] in use, as if downloaded in Settings → AI model; before the chat shows, which
     * looks at the model then. A Notekea started again on the same data keeps it.
     */
    fun useChatModel() {
        val size = requireNotNull(ChatModels.preferred()) { "No chat model is on this machine" }

        ChatModels.install(
            size = size,
            folder = koin.get<ModelFolder>().root
        )

        runBlocking { koin.get<ActiveModelStore>().save(size = size) }
    }

    /** The sync of the vault [name], for the performance runs that time it without the UI. */
    fun syncOf(name: String): VaultSync =
        koin.get<VaultSyncs>().of(vaultPath = vaultPath(name = name))

    /** Where the vault [name] is. */
    fun vaultPath(name: String): String = vaults.resolve(name).toString()

    @Composable
    fun Content() {
        val environment = remember { koin.get<AppEnvironment>() }

        NotekeaAppLocals(
            environment = environment,
            initialAppearance = Appearance()
        ) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModels,
                LocalUriHandler provides uriHandler
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onPreviewKeyEvent(onPreviewKeyEvent = shortcuts::dispatch)
                        .focusRequester(focusRequester = windowFocus)
                        .focusable()
                ) {
                    // Closing the window quits, as in the app: the case's Notekea quits and starts again (1.1-07).
                    NotekeaRoot(
                        host = host,
                        onClose = {
                            CaseApps.quit(
                                vaults = vaults,
                                whileClosed = {}
                            )
                        }
                    )
                }
            }
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
        single<TextClipboard> { clipboard }
        single<PageFetcher> { StandInDocsSite(real = KtorPageFetcher(dispatchers = get())) }

        single<Trash> {
            VaultFolderTrash(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = get()
            )
        }

        // The index's embedder (2.3): the bundled model in Notekea QA, a stand-in where it is not there (CI).
        if (!hasBundledModel()) {
            single<TextEmbedder> { StandInTextEmbedder() }
        }
    }

    private fun hasBundledModel(): Boolean {
        val resources = System.getProperty(RESOURCES_PROPERTY) ?: return false

        return Files.isRegularFile(
            Path.of(resources, LlamaTextEmbedder.MODEL_FOLDER, LlamaTextEmbedder.MODEL_FILE)
        )
    }

    private companion object {
        const val HOME = "home"
        const val DOCUMENTS = "Documents"
        const val RESOURCES_PROPERTY = "compose.application.resources.dir"
    }
}

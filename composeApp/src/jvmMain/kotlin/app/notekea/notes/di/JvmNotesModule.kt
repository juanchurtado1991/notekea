package app.notekea.notes.di

import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.notes.data.DirectoryWatcherFileWatcher
import app.notekea.notes.data.JvmSystemTrash
import app.notekea.notes.data.OkioNoteContentStore
import app.notekea.notes.data.ObsidianVaultSettingsReader
import app.notekea.notes.data.OkioAttachmentStore
import app.notekea.notes.data.OkioNoteFileOperations
import app.notekea.notes.data.OkioNoteMetadataReader
import app.notekea.notes.data.OkioNoteScanner
import app.notekea.notes.data.AwtClipboardReader
import app.notekea.notes.data.FileKitAttachmentPicker
import app.notekea.notes.data.ProcessFileOpener
import app.notekea.notes.data.ProcessFileRevealer
import app.notekea.notes.data.SystemFirstTrash
import app.notekea.notes.data.VaultFolderTrash
import app.notekea.notes.data.WaylandClipboardReader
import app.notekea.notes.data.readCommand
import app.notekea.notes.data.runCommand
import app.notekea.notes.domain.AttachmentPicker
import app.notekea.notes.domain.ClipboardReader
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.FileRevealer
import app.notekea.notes.domain.FileWatcher
import app.notekea.notes.domain.NoteContentStore
import app.notekea.notes.domain.NoteFileOperations
import app.notekea.notes.domain.NoteMetadataReader
import app.notekea.notes.domain.NoteScanner
import app.notekea.notes.domain.Trash
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.VaultSettingsReader
import okio.FileSystem
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

// On Wayland the owner of a copied image never answers Java, so it gives up after a second instead of ten.
private val WAYLAND_SELECTION_TIMEOUT = 1.seconds

/**
 * Desktop implementations of the notes ports: the real file system through Okio, native change events, the system
 * trash with the vault's `.trash` folder as fallback, and the system file manager.
 */
val jvmNotesModule = module {
    single<FileRevealer> {
        ProcessFileRevealer(
            operatingSystem = get<PlatformConventions>().operatingSystem,
            runCommand = ::runCommand,
            dispatchers = get()
        )
    }
    single<FileOpener> {
        ProcessFileOpener(
            operatingSystem = get<PlatformConventions>().operatingSystem,
            runCommand = ::runCommand,
            dispatchers = get()
        )
    }
    single<AttachmentPicker> { FileKitAttachmentPicker() }
    single<ClipboardReader> {
        val isWayland = get<PlatformConventions>().operatingSystem == OperatingSystem.LINUX &&
            get<SystemEnvironment>().environmentVariable(name = WaylandClipboardReader.WAYLAND_DISPLAY) != null
        if (isWayland) {
            WaylandClipboardReader(
                primary = AwtClipboardReader(selectionTimeout = WAYLAND_SELECTION_TIMEOUT),
                readCommand = ::readCommand
            )
        } else {
            AwtClipboardReader()
        }
    }
    single<FileWatcher> { DirectoryWatcherFileWatcher(dispatchers = get()) }
    single<NoteContentStore> { OkioNoteContentStore(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<NoteFileOperations> { OkioNoteFileOperations(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<AttachmentStore> { OkioAttachmentStore(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<VaultSettingsReader> { ObsidianVaultSettingsReader(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<NoteMetadataReader> {
        OkioNoteMetadataReader(fileSystem = FileSystem.SYSTEM, dispatchers = get(), parser = get())
    }
    single<NoteScanner> { OkioNoteScanner(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<Trash> {
        val systemTrash =
            JvmSystemTrash(operatingSystem = get<PlatformConventions>().operatingSystem, runCommand = ::runCommand)
        SystemFirstTrash(
            moveToSystemTrash = systemTrash::moveToTrash,
            vaultTrash = VaultFolderTrash(fileSystem = FileSystem.SYSTEM, dispatchers = get()),
            dispatchers = get()
        )
    }
}

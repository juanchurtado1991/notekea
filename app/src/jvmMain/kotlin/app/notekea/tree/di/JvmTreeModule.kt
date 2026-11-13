package app.notekea.tree.di

import app.notekea.core.notes.data.JvmSystemTrash
import app.notekea.core.platform.data.runCommand
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.tree.data.DirectoryWatcherFileWatcher
import app.notekea.tree.data.OkioNoteMetadataReader
import app.notekea.tree.data.ProcessFileRevealer
import app.notekea.tree.data.SystemFirstTrash
import app.notekea.tree.data.VaultFolderTrash
import app.notekea.tree.domain.FileRevealer
import app.notekea.tree.domain.FileWatcher
import app.notekea.tree.domain.NoteMetadataReader
import app.notekea.tree.domain.Trash
import okio.FileSystem
import org.koin.dsl.module

/**
 * Desktop implementations of the tree's ports: native change events, the system file manager and the system
 * trash, with the vault's `.trash` folder as fallback.
 */
val jvmTreeModule = module {
    single<FileRevealer> {
        ProcessFileRevealer(
            operatingSystem = get<PlatformConventions>().operatingSystem,
            runCommand = ::runCommand,
            dispatchers = get()
        )
    }

    single<FileWatcher> { DirectoryWatcherFileWatcher(dispatchers = get()) }

    single<NoteMetadataReader> {
        OkioNoteMetadataReader(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get(),
            parser = get()
        )
    }

    single<Trash> {
        val systemTrash = JvmSystemTrash(
            operatingSystem = get<PlatformConventions>().operatingSystem,
            runCommand = ::runCommand
        )

        SystemFirstTrash(
            moveToSystemTrash = systemTrash::moveToTrash,
            vaultTrash = VaultFolderTrash(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = get()
            ),
            dispatchers = get()
        )
    }
}

package app.notekea.vault.di

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.vault.data.FileKitFolderPicker
import app.notekea.vault.data.OkioVaultFileSystem
import app.notekea.vault.domain.FolderPicker
import app.notekea.vault.domain.VaultFileSystem
import okio.FileSystem
import org.koin.dsl.module

/** Desktop implementations of the vault ports: the real file system and FileKit's native folder picker. */
val jvmVaultModule = module {
    single<FolderPicker> { FileKitFolderPicker() }
    single<VaultFileSystem> {
        OkioVaultFileSystem(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get(),
            homeDirectory = get<PlatformConventions>().homeDirectory
        )
    }
}

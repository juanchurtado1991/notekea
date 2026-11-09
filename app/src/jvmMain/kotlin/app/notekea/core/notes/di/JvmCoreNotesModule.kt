package app.notekea.core.notes.di

import app.notekea.core.notes.data.LuceneVectorIndex
import app.notekea.core.notes.data.NioNoteFileAttributes
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioAttachmentStore
import app.notekea.core.notes.data.OkioNoteContentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.data.OkioNoteScanner
import app.notekea.core.notes.data.ProcessFileOpener
import app.notekea.core.notes.data.runCommand
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NoteScanner
import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.core.notes.domain.VectorIndex
import app.notekea.core.platform.domain.PlatformConventions
import java.nio.file.Path
import okio.FileSystem
import org.koin.dsl.module

private const val INDEX_FOLDER = "index"

/**
 * Desktop implementations of the ports features share: the real file system through Okio, the system file
 * opener and the search indexes on Lucene.
 */
val jvmCoreNotesModule = module {
    // The search indexes (ROADMAP.md 2.3), one per vault, in the app data's `index` folder: never inside a vault.
    single<VectorIndex> {
        LuceneVectorIndex(
            indexRoot = Path.of(get<PlatformConventions>().appDataDirectory, INDEX_FOLDER).toString(),
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
    single<NoteContentStore> {
        OkioNoteContentStore(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get(),
            attributes = NioNoteFileAttributes()
        )
    }
    single<NoteFileOperations> { OkioNoteFileOperations(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<AttachmentStore> { OkioAttachmentStore(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<VaultSettingsReader> { ObsidianVaultSettingsReader(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
    single<NoteScanner> { OkioNoteScanner(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
}

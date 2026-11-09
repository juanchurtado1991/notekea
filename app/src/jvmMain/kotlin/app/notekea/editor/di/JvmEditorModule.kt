package app.notekea.editor.di

import app.notekea.core.notes.data.readCommand
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.editor.data.AwtClipboardReader
import app.notekea.editor.data.FileKitAttachmentPicker
import app.notekea.editor.data.OkioNoteTimesReader
import app.notekea.editor.data.WaylandClipboardReader
import app.notekea.editor.domain.AttachmentPicker
import app.notekea.editor.domain.ClipboardReader
import app.notekea.editor.domain.NoteTimesReader
import kotlin.time.Duration.Companion.seconds
import okio.FileSystem
import org.koin.dsl.module

// On Wayland the owner of a copied image never answers Java, so it gives up after a second instead of ten.
private val WAYLAND_SELECTION_TIMEOUT = 1.seconds

/**
 * Desktop implementations of the editor's ports: the file picker, the clipboard and the note's dates.
 */
val jvmEditorModule = module {
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
    single<NoteTimesReader> { OkioNoteTimesReader(fileSystem = FileSystem.SYSTEM, dispatchers = get()) }
}

package app.notekea.editor.di

import app.notekea.core.platform.data.readCommand
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.editor.data.AwtClipboardReader
import app.notekea.editor.data.FfmpegInstaller
import app.notekea.editor.data.FfmpegTool
import app.notekea.editor.data.FfmpegVideoCompressor
import app.notekea.editor.data.FfmpegVideoFrames
import app.notekea.editor.data.FileKitAttachmentPicker
import app.notekea.editor.data.JvmLocalTimeFormatter
import app.notekea.editor.data.KtorYouTubeDetails
import app.notekea.editor.data.OkioNoteTimesReader
import app.notekea.editor.data.WaylandClipboardReader
import app.notekea.editor.domain.AttachmentPicker
import app.notekea.editor.domain.ClipboardReader
import app.notekea.editor.domain.LocalTimeFormatter
import app.notekea.editor.domain.NoteTimesReader
import app.notekea.editor.domain.VideoCompressor
import app.notekea.editor.domain.YouTubeDetails
import okio.FileSystem
import org.koin.dsl.module
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

// On Wayland the owner of a copied image never answers Java, so it gives up after a second instead of ten.
private val WAYLAND_SELECTION_TIMEOUT = 1.seconds

// The pictures of YouTube videos pasted into notes, kept so a note never asks YouTube again (#141).
private const val YOUTUBE_FOLDER = "youtube"

// FFmpeg, downloaded the first time a video needs making smaller, and the copies it makes until they are attached.
private const val FFMPEG_FOLDER = "ffmpeg"
private const val COMPRESSING_FOLDER = "compressing"

/**
 * Desktop implementations of the editor's ports: the file picker, the clipboard, the note's dates and YouTube's
 * details of a pasted video.
 */
val jvmEditorModule = module {
    single<AttachmentPicker> { FileKitAttachmentPicker() }
    single<LocalTimeFormatter> { JvmLocalTimeFormatter() }

    single {
        val appData = get<PlatformConventions>().appDataDirectory

        val system = when (get<PlatformConventions>().operatingSystem) {
            OperatingSystem.LINUX -> "linux"
            OperatingSystem.MACOS -> "macosx"
            OperatingSystem.WINDOWS -> "windows"
        }

        FfmpegInstaller(
            folder = Path.of(appData, FFMPEG_FOLDER),
            tool = FfmpegTool.of(
                system = system,
                architecture = System.getProperty("os.arch").orEmpty()
            )
        )
    }

    single<VideoCompressor> {
        FfmpegVideoCompressor(
            installer = get(),
            workFolder = Path.of(get<PlatformConventions>().appDataDirectory, COMPRESSING_FOLDER),
            dispatchers = get()
        )
    }

    single {
        FfmpegVideoFrames(
            installer = get(),
            dispatchers = get()
        )
    }

    single<YouTubeDetails> {
        KtorYouTubeDetails(
            folder = Path.of(get<PlatformConventions>().appDataDirectory, YOUTUBE_FOLDER),
            dispatchers = get()
        )
    }

    single<ClipboardReader> {
        val isWayland = get<PlatformConventions>().operatingSystem == OperatingSystem.LINUX &&
            get<SystemEnvironment>().environmentVariable(
                name = WaylandClipboardReader.WAYLAND_DISPLAY
            ) != null

        if (isWayland) {
            WaylandClipboardReader(
                primary = AwtClipboardReader(selectionTimeout = WAYLAND_SELECTION_TIMEOUT),
                readCommand = ::readCommand
            )
        } else {
            AwtClipboardReader()
        }
    }

    single<NoteTimesReader> {
        OkioNoteTimesReader(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get()
        )
    }
}

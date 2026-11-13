package app.notekea.di

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.documents.domain.PdfPages
import app.notekea.editor.data.DesktopFilePosters
import app.notekea.editor.data.FfmpegVideoFrames
import app.notekea.editor.domain.FilePosters
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.nio.file.Path

// The pictures of the PDFs and videos in notes (a first page, a frame), made once each.
private const val POSTERS_FOLDER = "posters"

// A page of 612 points drawn 245 dots wide: enough for its card, three times as sharp as it is shown.
private const val PAGE_SCALE = 0.4f

/**
 * The pictures of the files a note embeds (the developer's request of 2026-11-13), composed here because they join two
 * features: the editor's cards, the documents' PDF pages.
 */
val jvmPostersModule = module {
    single<FilePosters> { filePosters() }
}

/**
 * The desktop's [FilePosters]; without [withVideos] a video gets no picture, so the Auto-Test's cases, each in an
 * app data folder of its own, never download FFmpeg for one.
 */
fun Scope.filePosters(withVideos: Boolean = true): FilePosters {
    val pdfPages = get<PdfPages>()
    val frames = get<FfmpegVideoFrames>()

    return DesktopFilePosters(
        folder = Path.of(get<PlatformConventions>().appDataDirectory, POSTERS_FOLDER),
        pdfPage = { vaultPath, path ->
            firstPage(
                pdfPages = pdfPages,
                vaultPath = vaultPath,
                path = path
            )
        },
        videoFrame = { file, seconds ->
            if (withVideos) {
                frames.frameAt(
                    file = file,
                    seconds = seconds
                )
            } else {
                null
            }
        },
        dispatchers = get()
    )
}

// The PDF's first page as a PNG; the PDF is let go at once, its tab opens it again if it shows.
private suspend fun firstPage(
    pdfPages: PdfPages,
    vaultPath: String,
    path: RelativePath
): ByteArray? {
    val page = pdfPages.render(
        vaultPath = vaultPath,
        path = path,
        page = 0,
        scale = PAGE_SCALE
    )

    pdfPages.release(
        vaultPath = vaultPath,
        path = path
    )

    return (page as? Outcome.Success)?.value
}

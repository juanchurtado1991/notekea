package app.notekea.documents.di

import app.notekea.documents.data.PdfBoxPages
import app.notekea.documents.domain.PdfPages
import org.koin.dsl.module

/** A PDF's real pages, drawn with PDFBox. */
val jvmDocumentsModule = module {
    single<PdfPages> { PdfBoxPages(dispatchers = get()) }
}

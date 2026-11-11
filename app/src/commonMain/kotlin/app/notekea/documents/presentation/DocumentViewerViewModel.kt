package app.notekea.documents.presentation

import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.documents.domain.DocumentReader
import app.notekea.core.documents.domain.DocumentText
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import app.notekea.core.presentation.designsystem.images.decodedImage
import app.notekea.documents.domain.PdfPages
import co.touchlab.kermit.Logger

/**
 * A document of the vault at [vaultPath] in a read-only tab (4.5): a PDF as its real pages, drawn as they come into
 * view, any other document as its text; a cited passage is scrolled to and marked, on its page for a PDF. "Open in the
 * system's app" opens it with [fileOpener].
 */
class DocumentViewerViewModel(
    private val vaultPath: String,
    private val path: RelativePath,
    private val reader: DocumentReader,
    private val pdfPages: PdfPages,
    private val fileOpener: FileOpener,
    private val dispatchers: AppDispatchers
) : BaseViewModel<DocumentViewerState, DocumentViewerIntent, Nothing>(
    initialState = DocumentViewerState(
        name = path.name,
        format = DocumentFormat.of(path = path)
    )
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var text: DocumentText? = null
    private val drawing = mutableSetOf<Int>()

    init {
        launchGuarded(onError = ::logError) { load() }
    }

    override fun handleIntent(intent: DocumentViewerIntent) {
        when (intent) {
            is DocumentViewerIntent.OnReveal -> launchGuarded(onError = ::logError) {
                reveal(
                    range = intent.range,
                    count = intent.count
                )
            }

            is DocumentViewerIntent.OnPageShown -> draw(page = intent.page)

            DocumentViewerIntent.OnOpenInApp -> launchGuarded(onError = ::logError) {
                fileOpener.open(
                    vaultPath = vaultPath,
                    path = path
                )
            }
        }
    }

    private suspend fun load() {
        val read = reader.read(
            vaultPath = vaultPath,
            path = path
        )

        val pageCount = if (state.value.format?.isPaged == true) {
            (
                pdfPages.count(
                    vaultPath = vaultPath,
                    path = path
                ) as? Outcome.Success
                )?.value ?: 0
        } else {
            0
        }

        when (read) {
            is Outcome.Success -> {
                text = read.value

                reduce {
                    it.copy(
                        isLoading = false,
                        blocks = DocumentBlock.of(text = read.value.text),
                        pageCount = pageCount
                    )
                }
            }

            // A scanned PDF has no text, yet its pages can still be looked at.
            is Outcome.Failure -> reduce {
                it.copy(
                    isLoading = false,
                    error = read.error,
                    pageCount = pageCount
                )
            }
        }

        state
            .value
            .shown
            ?.let {
                reveal(
                    range = it.range,
                    count = it.count
                )
            }
    }

    private suspend fun reveal(
        range: IntRange,
        count: Int
    ) {
        val read = text

        if (read == null || !state.value.isPaged) {
            return reduce {
                it.copy(
                    shown = ShownPassage(
                        range = range,
                        page = null,
                        marks = emptyList(),
                        count = count
                    )
                )
            }
        }

        val page = read
            .partAt(offset = range.first)
            ?.number
            ?.minus(1)

        val passage = read.text.substring(
            range.first.coerceAtLeast(minimumValue = 0),
            (range.last + 1).coerceAtMost(maximumValue = read.text.length)
        )

        val marks = page?.let {
            pdfPages.marks(
                vaultPath = vaultPath,
                path = path,
                page = it,
                passage = passage
            )
        }.orEmpty()

        reduce {
            it.copy(
                shown = ShownPassage(
                    range = range,
                    page = page,
                    marks = marks,
                    count = count
                )
            )
        }
    }

    private fun draw(page: Int) {
        val isDrawn = page in state.value.pages || !drawing.add(page)
        if (isDrawn) return

        launchGuarded(onError = ::logError) {
            val png = pdfPages.render(
                vaultPath = vaultPath,
                path = path,
                page = page,
                scale = PAGE_SCALE
            )

            val image = (png as? Outcome.Success)?.value?.let {
                decodedImage(
                    bytes = it,
                    dispatcher = dispatchers.io
                )
            }

            reduce { current ->
                // Only the pages near the one read stay drawn, so a long PDF does not fill the memory.
                val kept = current.pages.filterKeys {
                    kotlin.math.abs(it - page) <= KEPT_PAGES
                }

                current.copy(
                    pages = image?.let {
                        kept + (page to it)
                    } ?: kept
                )
            }

            drawing -= page
        }
    }

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "DocumentViewerViewModel"
        const val FAILED_MESSAGE = "The document could not be shown"
        const val PAGE_SCALE = 1.5f
        const val KEPT_PAGES = 6
    }
}

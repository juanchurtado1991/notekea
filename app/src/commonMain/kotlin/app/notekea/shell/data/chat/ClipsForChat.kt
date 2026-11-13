package app.notekea.shell.data.chat

import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.PageProgress
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.SectionAsked
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.clips.domain.ArticleExtractor
import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.ClipSectionUseCase
import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.DescribeSectionUseCase
import app.notekea.clips.domain.FindSectionUseCase
import app.notekea.clips.domain.PageFetcher
import app.notekea.clips.domain.SectionStep
import app.notekea.clips.domain.WebClipNote
import app.notekea.clips.domain.WebClipSection
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import kotlin.time.Clock

/** The chat's [WebPageClipper] and [PageReader], made of the clips feature (4.4): pages clipped, or only read. */
class ClipsForChat(
    private val clipWebPage: ClipWebPageUseCase,
    private val findSection: FindSectionUseCase,
    private val clipSection: ClipSectionUseCase,
    private val describeSection: DescribeSectionUseCase,
    private val fetcher: PageFetcher,
    private val extractor: ArticleExtractor,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : WebPageClipper,
    PageReader {
    /** A page's article for agent mode (4.7): downloaded and taken out, no notes written. */
    override suspend fun read(url: String): Outcome<String, PageProblem> {
        val page = when (val fetched = fetcher.fetch(url = url)) {
            is Outcome.Success -> fetched.value
            is Outcome.Failure -> return Outcome.Failure(error = problemOf(error = fetched.error))
        }

        return when (val article = extractor.extract(page = page)) {
            is Outcome.Success -> Outcome.Success(value = "${article.value.title}\n\n${article.value.text}")
            is Outcome.Failure -> Outcome.Failure(error = problemOf(error = article.error))
        }
    }

    override suspend fun clip(url: String): Outcome<NoteDraft, PageProblem> {
        var last: ClipStep = ClipStep.Fetching
        clipWebPage(url = url).collect { last = it }

        return when (val step = last) {
            is ClipStep.Done -> Outcome.Success(
                value = NoteDraft(
                    path = WebClipNote.pathOf(title = step.clip.title),
                    text = WebClipNote.textOf(
                        clip = step.clip,
                        today = today()
                    )
                )
            )

            is ClipStep.Failed -> Outcome.Failure(error = problemOf(error = step.error))

            else -> Outcome.Failure(error = PageProblem.REFUSED)
        }
    }

    override suspend fun sectionOf(
        url: String,
        language: String?
    ): Outcome<SectionAsked, PageProblem> = when (val found = findSection(url = url)) {
        is Outcome.Success -> Outcome.Success(
            value = SectionAsked(
                url = url,
                prefix = found.value.prefix,
                count = found.value.urls.size,
                more = found.value.more,
                canGoDeeper = found.value.canGoDeeper,
                summary = found
                    .value
                    .urls
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        describeSection(
                            links = found.value,
                            language = language
                        )
                    }
            )
        )

        is Outcome.Failure -> Outcome.Failure(error = problemOf(error = found.error))
    }

    override suspend fun clipSection(
        url: String,
        isEvery: Boolean,
        depth: Int,
        onProgress: suspend (PageProgress) -> Unit
    ): Outcome<List<NoteDraft>, PageProblem> {
        val found = findSection(
            url = url,
            depth = depth,
            onFound = { onProgress(PageProgress(found = it)) }
        )

        val links = when (found) {
            is Outcome.Success -> if (isEvery) found.value.everyPage() else found.value
            is Outcome.Failure -> return Outcome.Failure(error = problemOf(error = found.error))
        }

        var last: SectionStep? = null

        clipSection(
            url = url,
            links = links
        ).collect { step ->
            last = step

            if (step is SectionStep.Clipping) {
                onProgress(
                    PageProgress(
                        done = step.done,
                        total = step.total,
                        url = step.url
                    )
                )
            }
        }

        return when (val step = last) {
            is SectionStep.Done -> Outcome.Success(
                value = WebClipSection
                    .notesOf(
                        done = step.copy(summary = describeSection(links = links)),
                        today = today()
                    )
                    .map { (path, text) ->
                        NoteDraft(
                            path = path,
                            text = text
                        )
                    }
            )

            is SectionStep.Failed -> Outcome.Failure(error = problemOf(error = step.error))

            else -> Outcome.Failure(error = PageProblem.REFUSED)
        }
    }

    private fun today(): String {
        val date = localCalendar.momentOf(instant = clock.now()).date

        return "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
    }

    private fun problemOf(error: ClipError): PageProblem = when (error) {
        ClipError.BadAddress -> PageProblem.BAD_ADDRESS
        ClipError.LocalAddress -> PageProblem.LOCAL_ADDRESS
        ClipError.Offline -> PageProblem.OFFLINE
        ClipError.TooSlow -> PageProblem.TOO_SLOW
        ClipError.TooLarge -> PageProblem.TOO_LARGE
        is ClipError.Refused -> PageProblem.REFUSED
        ClipError.NotArticle -> PageProblem.NOT_ARTICLE
    }
}

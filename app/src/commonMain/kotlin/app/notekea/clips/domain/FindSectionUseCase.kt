package app.notekea.clips.domain

import app.notekea.core.domain.Outcome

/** The page at a URL downloaded to find the pages it links under its own path ([SectionLinks]), before clipping. */
class FindSectionUseCase(private val fetcher: PageFetcher) {
    suspend operator fun invoke(url: String): Outcome<SectionLinks, ClipError> =
        when (val fetched = fetcher.fetch(url = url.trim())) {
            is Outcome.Success -> Outcome.Success(value = SectionLinks.of(page = fetched.value))
            is Outcome.Failure -> fetched
        }
}

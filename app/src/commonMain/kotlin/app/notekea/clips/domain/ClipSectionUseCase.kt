package app.notekea.clips.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A page and the pages it links under its own path ([SectionLinks]), clipped one by one as [ClipWebPageUseCase] clips
 * one, the person told how far it is; a linked page that cannot be clipped is skipped and named, the page must not.
 */
class ClipSectionUseCase(private val clipWebPage: ClipWebPageUseCase) {
    operator fun invoke(
        url: String,
        links: SectionLinks
    ): Flow<SectionStep> = flow {
        val total = links.urls.size + 1
        val clips = mutableListOf<WebClip>()
        val skipped = mutableListOf<String>()

        (listOf(url) + links.urls).forEachIndexed { index, page ->
            emit(
                value = SectionStep.Clipping(
                    done = index,
                    total = total,
                    url = page
                )
            )

            var last: ClipStep = ClipStep.Fetching
            clipWebPage(url = page).collect { last = it }

            when (val step = last) {
                is ClipStep.Done -> clips += step.clip

                is ClipStep.Failed -> {
                    if (index == 0) return@flow emit(SectionStep.Failed(step.error))
                    skipped += page
                }

                else -> skipped += page
            }
        }

        emit(
            value = SectionStep.Done(
                main = clips.first(),
                pages = clips.drop(n = 1),
                skipped = skipped,
                about = links.about
            )
        )
    }
}

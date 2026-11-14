package app.notekea.shell.data.chat

import app.notekea.chat.domain.plan.PageProgress
import app.notekea.chat.domain.sources.ClippedPage
import app.notekea.chat.domain.sources.SourcePage
import app.notekea.chat.domain.sources.SourceSites
import app.notekea.clips.domain.FoundSourcePage
import app.notekea.clips.domain.WebSourceSearch
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.RelativePath
import kotlin.time.Clock

/** The chat's [SourceSites], made of the clips feature: the sites of the vault's web sources, searched and clipped. */
class SourceSitesForChat(
    private val search: WebSourceSearch,
    private val noteContentStore: NoteContentStore,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : SourceSites {
    override suspend fun sitesOf(vaultPath: String): List<String> =
        search.sites(vaultPath = vaultPath)

    override suspend fun pagesFor(
        vaultPath: String,
        question: String,
        onProgress: suspend (PageProgress) -> Unit
    ): List<SourcePage> = search
        .pages(
            vaultPath = vaultPath,
            question = question,
            onFound = { onProgress(PageProgress(found = it)) }
        )
        .map {
            SourcePage(
                url = it.url,
                about = it.about,
                section = it.section.value
            )
        }

    override suspend fun clip(
        vaultPath: String,
        pages: List<SourcePage>,
        onProgress: suspend (PageProgress) -> Unit
    ): List<ClippedPage> {
        var done = 0

        suspend fun told(url: String) {
            onProgress(
                PageProgress(
                    done = done,
                    total = pages.size,
                    url = url
                )
            )

            done++
        }

        val written = search.add(
            vaultPath = vaultPath,
            pages = pages.map {
                FoundSourcePage(
                    url = it.url,
                    about = it.about,
                    section = RelativePath(value = it.section)
                )
            },
            today = today(),
            onPage = { told(url = it) }
        )

        return written.mapNotNull { path ->
            noteContentStore
                .read(
                    vaultPath = vaultPath,
                    path = path
                )
                ?.let {
                    ClippedPage(
                        path = path,
                        text = it
                    )
                }
        }
    }

    private fun today(): String {
        val date = localCalendar.momentOf(instant = clock.now()).date

        val month = date
            .month
            .toString()
            .padStart(
                length = 2,
                padChar = '0'
            )

        val day = date
            .day
            .toString()
            .padStart(
                length = 2,
                padChar = '0'
            )

        return "${date.year}-$month-$day"
    }
}

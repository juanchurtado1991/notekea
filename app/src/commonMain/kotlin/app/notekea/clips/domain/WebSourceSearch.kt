package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.TextEmbedder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.WebSourcesFolder

/**
 * When the notes do not hold an answer (the developer's request of 2026-11-13): the pages of the sites the vault
 * has web sources of that were not clipped yet and are closest to the question, and those the person picks clipped
 * into their sections. It looks only in sites already clipped from, never the web at large.
 */
class WebSourceSearch(
    private val update: UpdateWebSourcesUseCase,
    private val findSection: FindSectionUseCase,
    private val textEmbedder: TextEmbedder
) {
    /** The sites ("wordpress.org") the vault has sections of web sources of. */
    suspend fun sites(vaultPath: String): List<String> = sectionsOf(vaultPath = vaultPath)
        .mapNotNull { it.index?.source }
        .map { hostOf(url = it) }
        .distinct()

    /** At most [MOST] pages not clipped yet, the closest to [question] first; [onFound] told how many it weighs. */
    suspend fun pages(
        vaultPath: String,
        question: String,
        onFound: suspend (pages: Int) -> Unit = {}
    ): List<FoundSourcePage> {
        val candidates = mutableListOf<FoundSourcePage>()

        sectionsOf(vaultPath = vaultPath).take(n = MOST_SECTIONS).forEach { section ->
            candidates += unclipped(section = section)
            onFound(candidates.size)
        }

        if (candidates.isEmpty()) return emptyList()

        return closest(
            question = question,
            candidates = candidates.distinctBy { WebSourceNote.keyOf(url = it.url) }
        )
    }

    /** [pages] clipped into their sections, without the model, each told to [onPage] first; the notes written. */
    suspend fun add(
        vaultPath: String,
        pages: List<FoundSourcePage>,
        today: String,
        onPage: suspend (url: String) -> Unit = {}
    ): List<RelativePath> {
        val sections = sectionsOf(vaultPath = vaultPath).associateBy { it.index?.path }

        return pages
            .groupBy { it.section }
            .flatMap { (index, chosen) ->
                val section = sections[index] ?: return@flatMap emptyList()

                UpdatedSection(
                    section = section,
                    tools = update,
                    vaultPath = vaultPath,
                    today = today
                ).add(
                    urls = chosen.map { it.url },
                    onPage = onPage
                )
            }
    }

    // The sections clipped with their pages, the largest first: a page clipped alone has no site to look in.
    private suspend fun sectionsOf(vaultPath: String): List<SourceSection> = update
        .sectionsOf(
            vaultPath = vaultPath,
            target = WebSourcesFolder.PATH
        )
        .filter { section ->
            val index = section.index
            index != null && (section.notes.size > 1 || WebSourceNote.hasPages(text = index.text))
        }
        .sortedByDescending { it.notes.size }

    // The pages the section's site has now, a level past what was clipped, that no note came from.
    private suspend fun unclipped(section: SourceSection): List<FoundSourcePage> {
        val index = section.index ?: return emptyList()

        val known = section
            .notes
            .map { WebSourceNote.keyOf(url = it.source) }
            .toSet()

        val levels = WebSourceNote.levelsOf(text = index.text) + 1

        val found = findSection(
            url = index.source,
            depth = levels.coerceAtMost(maximumValue = SectionLinks.MAX_DEPTH),
            most = MOST_WEIGHED
        ) as? Outcome.Success ?: return emptyList()

        val links = found.value

        return (links.urls + links.rest)
            .filter {
                WebSourceNote.keyOf(url = it) !in known
            }
            .map {
                FoundSourcePage(
                    url = it,
                    about = links.aboutOf(url = it),
                    section = index.path
                )
            }
    }

    // By meaning, with the model the notes are searched with; by the words they share when it cannot say. Only
    // pages close to the best one: a page that merely exists answers nothing.
    private suspend fun closest(
        question: String,
        candidates: List<FoundSourcePage>
    ): List<FoundSourcePage> {
        val texts = listOf(question) + candidates.map { it.about }
        val vectors = (textEmbedder.embed(texts = texts) as? Outcome.Success)?.value

        val scores = if (vectors == null) {
            candidates.map {
                shared(
                    question = question,
                    about = it.about
                )
            }
        } else {
            val asked = vectors.first()

            vectors.drop(n = 1).map { vector ->
                asked.indices.sumOf { (asked[it] * vector[it]).toDouble() }
            }
        }

        val least = if (vectors == null) 1.0 else LEAST_ALIKE
        val best = scores.maxOrNull() ?: return emptyList()
        if (best < least) return emptyList()

        return candidates
            .zip(scores)
            .filter { (_, score) ->
                score >= least && score >= best - NEAR_BEST
            }
            .sortedByDescending { it.second }
            .take(n = MOST)
            .map { it.first }
    }

    private fun shared(
        question: String,
        about: String
    ): Double {
        val words = about
            .lowercase()
            .split(WORD)
            .filter {
                it.length > SHORT_WORD
            }
            .toSet()

        return question
            .lowercase()
            .split(WORD)
            .count {
                it in words
            }
            .toDouble()
    }

    private fun hostOf(url: String): String = url
        .substringAfter(delimiter = "://")
        .substringBefore(delimiter = '/')
        .removePrefix(prefix = "www.")

    companion object {
        const val MOST = 5
        private const val MOST_SECTIONS = 3
        private const val MOST_WEIGHED = 400
        private const val LEAST_ALIKE = 0.3
        private const val NEAR_BEST = 0.15
        private const val SHORT_WORD = 3
        private val WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")
    }
}

package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * One section of web sources updated ([UpdateWebSourcesUseCase]): its pages read again, cleaned together of what the
 * site repeats on each, compared with the notes, and the pages its index now links that no note has clipped as new.
 */
internal class UpdatedSection(
    private val section: SourceSection,
    private val tools: UpdateWebSourcesUseCase,
    private val vaultPath: String,
    private val today: String
) {
    suspend fun run(onPage: suspend (url: String) -> Unit): UpdateStep.Done {
        val clips = section.notes.associateWith { note ->
            onPage(note.source)
            tools.clipOf(url = note.source)
        }

        val fresh = newPages(onPage = onPage)
        val cleaned = cleaned(clips = clips.values.filterNotNull() + fresh)

        val updated = clips
            .mapNotNull { (note, clip) ->
                clip?.let {
                    note to (cleaned[it.url] ?: it)
                }
            }
            .filter { (note, clip) ->
                clip
                    .article
                    .text
                    .trim() != WebSourceNote.originalOf(text = note.text)
            }
            .filter { (note, clip) ->
                tools.write(
                    vaultPath = vaultPath,
                    path = note.path,
                    text = WebSourceNote.updated(
                        text = note.text,
                        article = clip.article.text,
                        today = today
                    )
                )
            }
            .map { (note, _) -> note.path }

        val added = added(
            clips = fresh.map {
                cleaned[it.url] ?: it
            }
        )

        val failed = clips.values.count {
            it == null
        }

        return UpdateStep.Done(
            updated = updated,
            added = added,
            unchanged = section.notes.size - updated.size - failed,
            failed = failed
        )
    }

    // The pages the index links now that no note of the section came from, clipped with their notes as a first clip.
    private suspend fun newPages(onPage: suspend (String) -> Unit): List<WebClip> {
        val index = section.index ?: return emptyList()

        // A page clipped alone stays alone: only a section, clipped with its pages, gets the new ones.
        val isSection = section.notes.size > 1 || WebClipSection.PAGES in index.text
        if (!isSection) return emptyList()

        val known = section
            .notes
            .map { keyOf(url = it.source) }
            .toSet()

        return tools
            .pagesOf(
                url = index.source,
                levels = WebSourceNote.levelsOf(text = index.text)
            )
            .filter {
                keyOf(url = it) !in known
            }
            .mapNotNull { url ->
                onPage(url)

                tools.clipOf(
                    url = url,
                    withNotes = true
                )
            }
    }

    // The clips by their address, without the site's menus and name, when they are a section and not a page alone.
    private fun cleaned(clips: List<WebClip>): Map<String, WebClip> {
        val main = clips.firstOrNull() ?: return emptyMap()

        val done = WebClipSection.cleaned(
            done = SectionStep.Done(
                main = main,
                pages = clips.drop(n = 1),
                skipped = emptyList()
            )
        )

        return (listOf(done.main) + done.pages).associateBy { it.url }
    }

    private suspend fun added(clips: List<WebClip>): List<RelativePath> {
        val index = section.index ?: return emptyList()
        if (clips.isEmpty()) return emptyList()

        val taken = section
            .notes
            .map {
                it
                    .path
                    .nameWithoutExtension
                    .lowercase()
            }
            .toMutableSet()

        val written = clips.mapNotNull { clip ->
            var name = WebClipNote.nameOf(title = clip.title)
            var number = 1
            val wanted = name

            while (!taken.add(name.lowercase())) {
                number++
                name = "$wanted $number"
            }

            val path = section.folder.child(name = "$name${RelativePath.MARKDOWN_EXTENSION}")

            val text = WebClipNote.textOf(
                clip = clip,
                today = today
            )

            (path to name).takeIf {
                tools.write(
                    vaultPath = vaultPath,
                    path = path,
                    text = text,
                    isNew = true
                )
            }
        }

        val links = written.map { (_, name) -> "${section.folder.name}/$name|$name" }

        val latest = tools.read(
            vaultPath = vaultPath,
            path = index.path
        ) ?: index.text

        tools.write(
            vaultPath = vaultPath,
            path = index.path,
            text = WebSourceNote.withPages(
                text = latest,
                links = links
            )
        )

        return written.map { it.first }
    }

    // An address however it is written: with or without its last slash, its ".md" or its "www.".
    private fun keyOf(url: String): String = url
        .substringBefore(delimiter = '#')
        .substringBefore(delimiter = '?')
        .removeSuffix(suffix = "/")
        .removeSuffix(suffix = ".md")
        .removeSuffix(suffix = ".html")
        .substringAfter(delimiter = "://")
        .removePrefix(prefix = "www.")
        .lowercase()
}

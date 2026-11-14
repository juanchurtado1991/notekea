package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * One section of web sources updated ([UpdateWebSourcesUseCase]): its pages read again, cleaned together of what the
 * site repeats on each, compared with the notes as they are when each is written, and the pages its index now links
 * that no note came from clipped as new, without the model.
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

        val fresh = newPages(
            read = clips.values.filterNotNull(),
            onPage = onPage
        )

        val cleaned = cleaned(clips = clips.values.filterNotNull() + fresh)
        var result = UpdateStep.Done()

        clips.forEach { (note, clip) ->
            val article = clip
                ?.let {
                    cleaned[it.url] ?: it
                }
                ?.article
                ?.text

            result = when (rewritten(note = note, article = article)) {
                true -> result.copy(updated = result.updated + note.path)
                false -> result.copy(unchanged = result.unchanged + 1)
                null -> result.copy(failed = result.failed + 1)
            }
        }

        return result.copy(
            added = added(
                clips = fresh.map {
                    cleaned[it.url] ?: it
                }
            )
        )
    }

    /** The pages at [urls] clipped into the section as new notes, each told to [onPage] first; their paths. */
    suspend fun add(
        urls: List<String>,
        onPage: suspend (url: String) -> Unit
    ): List<RelativePath> {
        val clips = urls.mapNotNull { url ->
            onPage(url)
            tools.clipOf(url = url)
        }

        return added(clips = clips)
    }

    // Whether the note was rewritten with [article]; null when its page could not be read, or reads as gone. The
    // note is read again here, minutes after the update began: what the person wrote meanwhile is what is kept.
    private suspend fun rewritten(
        note: SourceNote,
        article: String?
    ): Boolean? {
        article ?: return null

        val latest = tools.read(
            vaultPath = vaultPath,
            path = note.path
        ) ?: return null

        val isGone = WebSourceNote.looksGone(
            text = latest,
            article = article
        )

        if (isGone) return null

        val isSame = WebSourceNote.isSame(
            text = latest,
            article = article
        )

        if (isSame) return false

        val isWritten = tools.write(
            vaultPath = vaultPath,
            path = note.path,
            text = WebSourceNote.updated(
                text = latest,
                article = article,
                today = today
            )
        )

        return if (isWritten) true else null
    }

    // The pages the index links now that no note of the section came from. A link that only redirects to a page a
    // note has (/docs/latest/x to /docs/v3/x) is not new: its address is known once it is read.
    private suspend fun newPages(
        read: List<WebClip>,
        onPage: suspend (String) -> Unit
    ): List<WebClip> {
        val index = section.index ?: return emptyList()

        // A page clipped alone stays alone: only a section, clipped with its pages, gets the new ones.
        val isSection = section.notes.size > 1 || WebSourceNote.hasPages(text = index.text)
        if (!isSection) return emptyList()

        val known = (section.notes.map { it.source } + read.map { it.url })
            .map { WebSourceNote.keyOf(url = it) }
            .toMutableSet()

        val linked = tools
            .pagesOf(
                url = index.source,
                levels = WebSourceNote.levelsOf(text = index.text)
            )
            .filter {
                WebSourceNote.keyOf(url = it) !in known
            }
            .distinctBy { WebSourceNote.keyOf(url = it) }

        return linked.mapNotNull { url ->
            onPage(url)
            val clip = tools.clipOf(url = url) ?: return@mapNotNull null

            // Where the link ended, once followed: a page a note already has, or one two links led to.
            val isNew = known.add(WebSourceNote.keyOf(url = clip.url))
            known += WebSourceNote.keyOf(url = url)

            clip.takeIf { isNew }
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

    // Each new page as a note under a name no note of the folder has, the person's own included, then in the index.
    private suspend fun added(clips: List<WebClip>): List<RelativePath> {
        val index = section.index ?: return emptyList()
        if (clips.isEmpty()) return emptyList()
        val taken = section.names.toMutableSet()

        val written = clips.mapNotNull { clip ->
            val wanted = WebClipNote.nameOf(title = clip.title)
            var name = wanted
            var number = 1

            while (!taken.add(name.lowercase())) {
                number++
                name = "$wanted $number"
            }

            val path = section.folder.child(name = "$name${RelativePath.MARKDOWN_EXTENSION}")

            val isWritten = tools.create(
                vaultPath = vaultPath,
                path = path,
                text = WebClipNote.textOf(
                    clip = clip,
                    today = today
                )
            )

            (path to name).takeIf { isWritten }
        }

        val links = written.map { (_, name) -> "${section.folder.name}/$name|$name" }

        val latest = tools.read(
            vaultPath = vaultPath,
            path = index.path
        ) ?: return written.map { it.first }

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
}

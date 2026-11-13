package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * A clipped section as the notes it becomes: the page itself at `Web sources/<title>.md` with a "## Pages" list linking
 * each linked page, and those in `Web sources/<title>/`, one note each, under names no two share.
 */
object WebClipSection {
    const val PAGES = "## Pages"
    private const val SKIPPED = "Not clipped (no article found):"

    // Three pages at least, a paragraph on three in five of them is the site's, not the page's.
    private const val MIN_SHARED = 3
    private const val MENU_WORDS = 4
    private const val MENU_RUN = 8
    private const val SHARED_SHARE = 3
    private const val SHARED_SHARE_DIVISOR = 5
    private val SITE_SUFFIX = Regex(pattern = "\\s[-|–—]\\s[^-|–—]+$")

    /** Each note's path and text; [today] is the date they were clipped (YYYY-MM-DD). */
    fun notesOf(
        done: SectionStep.Done,
        today: String,
        levels: Int = 1
    ): List<Pair<RelativePath, String>> = notesOfCleaned(
        done = cleaned(done = done),
        today = today,
        levels = levels
    )

    /**
     * [done] without what every page repeats (the site's menu, read as text on some sites) and without the site's
     * name after each title ("Axiom - traceloop"): both found on the Traceloop docs (2026-11-11).
     */
    fun cleaned(done: SectionStep.Done): SectionStep.Done {
        val clips = listOf(done.main) + done.pages
        if (clips.size < MIN_SHARED) return done

        val shared = clips
            .flatMap {
                it
                    .article
                    .paragraphs
                    .distinct()
            }
            .groupingBy { it }
            .eachCount()
            .filterValues {
                it * SHARED_SHARE_DIVISOR >= clips.size * SHARED_SHARE
            }
            .keys

        val suffix = SITE_SUFFIX
            .find(input = done.main.title)
            ?.value
            ?.takeIf { suffix ->
                done.pages.any { it.title.endsWith(suffix = suffix) }
            }

        fun WebClip.trimmed() = copy(
            title = suffix?.let { title.removeSuffix(suffix = it) } ?: title,
            article = article.copy(
                paragraphs = withoutMenus(
                    paragraphs = article.paragraphs.filterNot {
                        it in shared
                    }
                )
                    .ifEmpty { article.paragraphs }
            )
        )

        return done.copy(
            main = done.main.trimmed(),
            pages = done.pages.map { it.trimmed() }
        )
    }

    // A run of many lines of a few words each is a menu read as text ("Community", "Support", "Start now", …).
    private fun withoutMenus(paragraphs: List<String>): List<String> {
        val isShort = paragraphs.map {
            it.split(' ').size <= MENU_WORDS
        }

        val kept = mutableListOf<String>()
        var start = 0

        while (start < paragraphs.size) {
            var end = start
            while (isSameRun(isShort = isShort, start = start, end = end)) end++

            val isMenu = isShort[start] && end - start >= MENU_RUN
            if (!isMenu) kept += paragraphs.subList(start, end)
            start = end
        }

        return kept
    }

    private fun isSameRun(
        isShort: List<Boolean>,
        start: Int,
        end: Int
    ): Boolean = end < isShort.size && isShort[end] == isShort[start]

    private fun notesOfCleaned(
        done: SectionStep.Done,
        today: String,
        levels: Int
    ): List<Pair<RelativePath, String>> {
        val folderName = WebClipNote.nameOf(title = done.main.title)
        val folder = WebClipNote.FOLDER.child(name = folderName)
        val taken = mutableSetOf<String>()

        val pages = done.pages.map { clip ->
            val name = uniqueName(
                wanted = WebClipNote.nameOf(title = clip.title),
                taken = taken
            )

            val path = folder.child(name = "$name${RelativePath.MARKDOWN_EXTENSION}")

            Triple(
                path,
                name,
                WebClipNote.textOf(
                    clip = clip,
                    today = today
                )
            )
        }

        // What each page is for, as the site's llms.txt says it: "Axiom: Send traces to Axiom" after its link.
        val purposes = done.pages.map { clip ->
            done
                .about[clip.url]
                ?.takeIf {
                    ": " in it
                }
                ?.substringAfter(delimiter = ": ")
        }

        val listed = buildString {
            append(PAGES).append("\n\n")
            done.summary?.let { append(it.trim()).append("\n\n") }

            pages.forEachIndexed { index, (_, name, _) ->
                append("- [[$folderName/$name|$name]]")
                purposes[index]?.let { append(" — ").append(it) }
                append('\n')
            }

            if (done.skipped.isNotEmpty()) {
                append("\n").append(SKIPPED).append("\n\n")
                done.skipped.forEach { append("- ").append(it).append('\n') }
            }
        }

        val index = WebClipNote.textOf(
            clip = done.main,
            today = today,
            before = listed,
            levels = levels
        )

        val main = WebClipNote.pathOf(title = done.main.title) to index

        return listOf(main) + pages.map { (path, _, text) ->
            path to text
        }
    }

    // "Axiom", then "Axiom 2" when two pages have one title.
    private fun uniqueName(
        wanted: String,
        taken: MutableSet<String>
    ): String {
        var name = wanted
        var number = 1

        while (!taken.add(name.lowercase())) {
            number++
            name = "$wanted $number"
        }

        return name
    }
}

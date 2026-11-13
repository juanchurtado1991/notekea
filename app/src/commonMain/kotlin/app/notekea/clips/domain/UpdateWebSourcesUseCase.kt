package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.WebSourcesFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Updates the web sources under a folder or a note (the developer's request of 2026-11-13): each clipped page is read
 * again and, when its article changed, the note's original text is replaced, what was written above it kept; a
 * section's pages that are new become notes in its folder and join its index. A page that cannot be read is left.
 */
class UpdateWebSourcesUseCase(
    private val clipWebPage: ClipWebPageUseCase,
    private val findSection: FindSectionUseCase,
    private val attachmentStore: AttachmentStore,
    private val noteContentStore: NoteContentStore,
    private val noteFileOperations: NoteFileOperations
) {
    operator fun invoke(
        vaultPath: String,
        target: RelativePath,
        today: String
    ): Flow<UpdateStep> = flow {
        val sections = sectionsOf(
            vaultPath = vaultPath,
            target = target
        )

        val total = sections.sumOf { it.notes.size }
        var done = 0
        var report = UpdateStep.Done()

        sections.forEach { section ->
            val result = UpdatedSection(
                section = section,
                tools = this@UpdateWebSourcesUseCase,
                vaultPath = vaultPath,
                today = today
            ).run { url ->
                emit(
                    value = UpdateStep.Reading(
                        done = done,
                        total = total.coerceAtLeast(done + 1),
                        url = url
                    )
                )

                done++
            }

            report = report.copy(
                updated = report.updated + result.updated,
                added = report.added + result.added,
                unchanged = report.unchanged + result.unchanged,
                failed = report.failed + result.failed
            )
        }

        emit(value = report)
    }

    /** The article of the page at [url] as a clip, without notes; null when it cannot be read. */
    internal suspend fun clipOf(
        url: String,
        withNotes: Boolean = false
    ): WebClip? {
        var last: ClipStep = ClipStep.Fetching

        clipWebPage(
            url = url,
            withNotes = withNotes
        ).collect { last = it }

        return (last as? ClipStep.Done)?.clip
    }

    /** The pages the section of the page at [url] has now, [levels] deep; none when it cannot be read. */
    internal suspend fun pagesOf(
        url: String,
        levels: Int
    ): List<String> = (
        findSection(
            url = url,
            depth = levels
        ) as? Outcome.Success
        )
        ?.value
        ?.urls
        .orEmpty()

    internal suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = noteContentStore.read(
        vaultPath = vaultPath,
        path = path
    )

    internal suspend fun write(
        vaultPath: String,
        path: RelativePath,
        text: String,
        isNew: Boolean = false
    ): Boolean {
        if (isNew) {
            path.parent?.let {
                noteFileOperations.createFolder(
                    vaultPath = vaultPath,
                    path = it
                )
            }

            noteFileOperations.createFile(
                vaultPath = vaultPath,
                path = path
            )
        }

        return noteContentStore.write(
            vaultPath = vaultPath,
            path = path,
            content = text
        ) is Outcome.Success
    }

    // The clipped notes [target] names, by section: a page clipped alone is a section of one, and a note inside a
    // section's folder stands for its whole section, whose pages are cleaned of the site's menus together.
    private suspend fun sectionsOf(
        vaultPath: String,
        target: RelativePath
    ): List<SourceSection> {
        if (!WebSourcesFolder.holds(path = target)) return emptyList()

        val files =
            (attachmentStore.files(vaultPath = vaultPath) as? Outcome.Success)?.value.orEmpty()

        val notes = files.filter {
            it.isMarkdown && it.isWithin(folder = WebSourcesFolder.PATH)
        }

        val isAll = target == WebSourcesFolder.PATH

        return notes
            .groupBy { keyOf(path = it) }
            .filter { (key, _) ->
                isAll || key == keyOf(path = target)
            }
            .map { (key, paths) ->
                val read = paths.mapNotNull { path ->
                    val text = read(
                        vaultPath = vaultPath,
                        path = path
                    ) ?: return@mapNotNull null

                    val source = WebSourceNote.sourceOf(text = text) ?: return@mapNotNull null

                    SourceNote(
                        path = path,
                        text = text,
                        source = source
                    )
                }

                SourceSection(
                    folder = WebSourcesFolder.PATH.child(name = key),
                    notes = read,
                    index = read.firstOrNull {
                        it.path.parent == WebSourcesFolder.PATH
                    }
                )
            }
            .filter { it.notes.isNotEmpty() }
    }

    // "Web sources/Docs.md" and "Web sources/Docs/Page.md" are both of the section "Docs".
    private fun keyOf(path: RelativePath): String {
        val inside =
            path.value.removePrefix(prefix = WebSourcesFolder.PATH.value + RelativePath.SEPARATOR)

        val first = inside.substringBefore(delimiter = RelativePath.SEPARATOR)

        val isInFolder = RelativePath.SEPARATOR in inside

        val note = first.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

        return if (isInFolder) first else note
    }
}

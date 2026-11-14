package app.notekea.chat.domain.plan

import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.WebSourcesFolder

/**
 * A note read with the documents it attaches (`![[contract.pdf]]`, a link to one): the chat works from what a pasted
 * PDF says, not from the line that embeds it (the developer's smoke of 2026-11-14: a table of a contract's pros and
 * cons was made up, the note read holding only the embed).
 */
internal class AttachedTexts(private val noteTextCache: NoteTextCache) {
    /** [note]'s text and, under a heading each, the text of the documents among [notes] it attaches. */
    suspend fun of(
        vaultPath: String,
        note: NoteEntry,
        notes: Collection<NoteEntry>
    ): ReadNote {
        val whole = textOf(
            vaultPath = vaultPath,
            note = note
        )

        // A clipped page is read from the page's own text: the notes a model wrote about it above are no source
        // to answer or write from either (code review, 2026-11-14).
        val original = WebSourcesFolder.originalStartIn(body = whole)
        val body = original?.let { whole.substring(startIndex = it) } ?: whole

        // What it attaches is looked for in all of it: a document pasted above a clipped page's text is attached too.
        val targets = targetsIn(body = whole)

        val documents = targets.mapNotNull { target ->
            notes.firstOrNull {
                it.file.path != note.file.path && isNamed(note = it, target = target)
            }
        }.distinct()

        val read = documents.map {
            it.file.path.name to textOf(
                vaultPath = vaultPath,
                note = it
            )
        }

        val known = documents
            .mapTo(destination = HashSet()) {
                it
                    .file
                    .path
                    .name
                    .lowercase()
            }

        // A document attached that is not in the vault, or one with no text in it. A link to a note whose name has
        // a dot ("Dr. Who") and a picture are no documents, and are not said to be unread ones.
        val unread = targets
            .map { it.substringAfterLast(delimiter = '/') }
            .filter {
                isDocument(name = it) && it.lowercase() !in known
            } +
            read.filter { it.second.isBlank() }.map { it.first }

        val attached = read.filter { it.second.isNotBlank() }

        // A note of embeds alone says nothing itself: with none of them read, it holds no text to work from.
        val own = body.takeIf {
            EMBED.replace(
                input = it,
                replacement = ""
            ).isNotBlank()
        }.orEmpty()

        val text = own.trim() + attached
            .joinToString(separator = "") { (name, text) ->
                "\n\n## Attached document: $name\n\n${text.trim()}"
            }

        return ReadNote(
            text = text.trim(),
            attached = attached.map { it.first },
            unread = unread.distinct()
        )
    }

    private suspend fun textOf(
        vaultPath: String,
        note: NoteEntry
    ): String = noteTextCache
        .textsOf(
            vaultPath = vaultPath,
            notes = listOf(note)
        )[note.file.path]
        ?.body
        .orEmpty()

    // The files a note embeds or links, as written: `![[a.pdf]]`, `[[Folder/a.pdf|the contract]]`, `[x](a%20b.pdf)`.
    private fun targetsIn(body: String): List<String> {
        val wiki = WIKI.findAll(input = body).map {
            it.groupValues[1]
        }

        val inline = INLINE.findAll(input = body).map {
            it
                .groupValues[1]
                .replace(
                    oldValue = "%20",
                    newValue = " "
                )
        }

        return (wiki + inline)
            .map { it.trim() }
            .filter { target ->
                val extension = target.substringAfterLast(
                    delimiter = '.',
                    missingDelimiterValue = ""
                )

                extension.isNotEmpty() &&
                    extension.length <= MAX_EXTENSION &&
                    !extension.equals(other = MARKDOWN, ignoreCase = true) &&
                    !target.contains(other = "://")
            }
            .distinct()
            .toList()
    }

    private fun isDocument(name: String): Boolean {
        val extension = name.substringAfterLast(delimiter = '.').lowercase()

        return DocumentFormat.entries.any {
            extension in it.extensions
        }
    }

    private fun isNamed(
        note: NoteEntry,
        target: String
    ): Boolean {
        val path = note.file.path
        if (DocumentFormat.of(path = path) == null) return false

        return path.value.equals(other = target, ignoreCase = true) ||
            path.name.equals(other = target.substringAfterLast(delimiter = '/'), ignoreCase = true)
    }

    private companion object {
        val WIKI = Regex(pattern = "\\[\\[([^\\]|#]+)")
        val INLINE = Regex(pattern = "]\\(<?([^)>\\s]+)>?\\)")
        val EMBED = Regex(pattern = "!?\\[\\[[^\\]]*]]|!?\\[[^\\]]*]\\([^)]*\\)")
        const val MARKDOWN = "md"
        const val MAX_EXTENSION = 5
    }
}

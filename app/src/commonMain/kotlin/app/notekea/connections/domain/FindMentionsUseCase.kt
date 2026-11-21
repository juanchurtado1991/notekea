package app.notekea.connections.domain

import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteText
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.LinkFinder
import app.notekea.core.notes.domain.links.LinkResolver
import app.notekea.core.notes.domain.links.linkSpan
import app.notekea.core.notes.domain.links.percentDecoded

/**
 * The notes that name [note], read from [texts]: those with a link that leads to it ([LinkResolver.closest]) and those
 * that only write its name or an alias as plain text ([MentionFinder]), never both, the note itself left out. Only
 * notes whose text holds the name are parsed, which keeps large vaults fast.
 */
class FindMentionsUseCase(
    private val linkFinder: LinkFinder,
    private val mentionFinder: MentionFinder
) {
    operator fun invoke(
        note: NoteEntry,
        notes: Collection<RelativePath>,
        texts: Map<RelativePath, NoteText>
    ): NoteMentions {
        val path = note.file.path

        val names = (listOf(path.nameWithoutExtension) + note.metadata.aliases)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

        val resolver = LinkResolver(notePaths = notes)
        val linked = mutableListOf<NoteMention>()
        val unlinked = mutableListOf<NoteMention>()

        texts.forEach { (source, text) ->
            val isSkipped = source == path || !mayName(
                body = text.body,
                names = names
            )

            if (isSkipped) return@forEach

            val links = linksTo(
                body = text.body,
                source = source,
                target = path,
                resolver = resolver
            )

            val places = links.ifEmpty {
                mentionFinder.find(
                    markdown = text.body,
                    names = names
                )
            }

            if (places.isEmpty()) return@forEach

            val mention = mentionOf(
                source = source,
                body = text.body,
                places = places
            )

            if (links.isEmpty()) unlinked += mention else linked += mention
        }

        return NoteMentions(
            linked = linked.sortedWith(order),
            unlinked = unlinked.sortedWith(order)
        )
    }

    /** Where [body] links to [target]: each whole link, from its first bracket (or `!`) to its last. */
    private fun linksTo(
        body: String,
        source: RelativePath,
        target: RelativePath,
        resolver: LinkResolver
    ): List<IntRange> = linkFinder
        .find(markdown = body)
        .filter {
            resolver.closest(
                link = it,
                source = source
            ) == target
        }
        .map {
            linkSpan(
                body = body,
                link = it
            )
        }

    /**
     * Whether [body] may name the note at all: as written, or percent-encoded in a Markdown link, as the app's own
     * rename writes `C%23%20%28draft%29.md`. Decoding runs only when the names are not there as written.
     */
    private fun mayName(
        body: String,
        names: List<String>
    ): Boolean {
        val isWritten = names.any {
            body.contains(
                other = it,
                ignoreCase = true
            )
        }

        if (isWritten) return true
        val decoded = percentDecoded(target = body)

        return names.any {
            decoded.contains(
                other = it,
                ignoreCase = true
            )
        }
    }

    private fun mentionOf(
        source: RelativePath,
        body: String,
        places: List<IntRange>
    ): NoteMention {
        val first = places.first()

        val (passage, highlight) = MentionPassage.of(
            body = body,
            range = first
        )

        return NoteMention(
            source = source,
            range = first,
            count = places.size,
            passage = passage,
            highlight = highlight
        )
    }

    private companion object {
        val names = NaturalNameComparator()
        val order = compareBy<NoteMention, String>(names) { it.source.nameWithoutExtension }
            .thenBy { it.source.value }
    }
}

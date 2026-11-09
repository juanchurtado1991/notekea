package app.notekea.connections.domain

import app.notekea.core.notes.domain.LinkFinder
import app.notekea.core.notes.domain.LinkKind
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteLink
import app.notekea.core.notes.domain.NoteText
import app.notekea.core.notes.domain.RelativePath

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

        val keys = names + path.nameWithoutExtension.replace(
            oldValue = " ",
            newValue = ENCODED_SPACE
        )

        val resolver = LinkResolver(notePaths = notes)
        val linked = mutableListOf<NoteMention>()
        val unlinked = mutableListOf<NoteMention>()

        texts.forEach { (source, text) ->
            val isSkipped = source == path || keys.none {
                text.body.contains(
                    other = it,
                    ignoreCase = true
                )
            }

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
            spanOf(
                body = body,
                link = it
            )
        }

    private fun spanOf(
        body: String,
        link: NoteLink
    ): IntRange = when (link.kind) {
        LinkKind.WIKI -> {
            val open = body.lastIndexOf(
                string = WIKI_OPEN,
                startIndex = link.targetStart
            )

            val start = if (open > 0 && body[open - 1] == EMBED) open - 1 else open

            start..(
                body.indexOf(
                    string = WIKI_CLOSE,
                    startIndex = link.targetEnd
                ) + 1
                )
        }

        LinkKind.MARKDOWN_ANGLE, LinkKind.MARKDOWN_ENCODED -> {
            val middle = body.lastIndexOf(
                string = MARKDOWN_MIDDLE,
                startIndex = link.targetStart
            )

            body.lastIndexOf(
                char = '[',
                startIndex = middle
            )..body.indexOf(
                char = ')',
                startIndex = link.targetEnd
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
        const val EMBED = '!'
        const val ENCODED_SPACE = "%20"
        const val MARKDOWN_MIDDLE = "]("
        const val WIKI_CLOSE = "]]"
        const val WIKI_OPEN = "[["
        val names = NaturalNameComparator()
        val order = compareBy<NoteMention, String>(names) { it.source.nameWithoutExtension }
            .thenBy { it.source.value }
    }
}

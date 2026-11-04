package app.notekea.notes.domain

import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.data.JetBrainsMarkdownMentionFinder
import app.notekea.notes.domain.NoteFixtures.entry
import kotlin.test.Test
import kotlin.test.assertEquals

class FindMentionsUseCaseTest {
    private val findMentions = FindMentionsUseCase(
        linkFinder = JetBrainsMarkdownLinkFinder(),
        mentionFinder = JetBrainsMarkdownMentionFinder()
    )

    @Test
    fun `wiki links, embeds and Markdown links to the note are linked mentions, by name, the link highlighted`() {
        val mentions = find(
            "Welcome.md" to "Weekly cooking lives in [[Arepas]] and [[Pasta]].",
            "Journal/Day.md" to
                "Breakfast: [the corn cakes](../Recipes/Arepas.md). Then ![[Arepas#Steps]] twice: [[Arepas]].",
            "Recipes/Arepas.md" to "I link to myself: [[Arepas]].",
            "Ideas.md" to "Nothing about it."
        )

        assertEquals(
            expected = listOf(
                mention(
                    source = "Journal/Day.md",
                    count = 3,
                    passage = "Breakfast: the corn cakes. Then Arepas#Steps twice: Arepas.",
                    lit = "the corn cakes"
                ),
                mention(
                    source = "Welcome.md",
                    count = 1,
                    passage = "Weekly cooking lives in Arepas and Pasta.",
                    lit = "Arepas"
                )
            ),
            actual = mentions.linked.map { it.copy(range = IntRange.EMPTY) }
        )
    }

    @Test
    fun `the name or an alias in plain words is an unlinked mention, never in code, links, addresses or tags`() {
        val mentions = find(
            "Lisbon.md" to "We found arepas by the river.",
            "Corn.md" to "Corn cakes, again.",
            "Code.md" to "`Arepas` and https://example.com/Arepas and #arepas and [Arepas](https://example.com).",
            "Linked.md" to "Arepas, see [[Arepas]].",
            "Prefix.md" to "Arepasx and xArepas are other words."
        )

        assertEquals(
            expected = Triple(
                listOf("Linked.md"),
                listOf(
                    mention(source = "Corn.md", count = 1, passage = "Corn cakes, again.", lit = "Corn cakes"),
                    mention(source = "Lisbon.md", count = 1, passage = "We found arepas by the river.", lit = "arepas")
                ),
                listOf(9..14, 0..9)
            ),
            actual = Triple(
                mentions.linked.map { it.source.value },
                mentions.unlinked.map { it.copy(range = IntRange.EMPTY) },
                mentions.unlinked.map { it.range }.reversed()
            )
        )
    }

    @Test
    fun `a long line is shortened around the mention`() {
        val before = "word ".repeat(n = 60)
        val mentions = find("Long.md" to before + "Arepas at last " + "tail ".repeat(n = 60))

        val shown = mentions.unlinked.single()

        assertEquals(
            expected = listOf(true, true, "Arepas"),
            actual = listOf(
                shown.passage.startsWith(prefix = "…") && shown.passage.endsWith(suffix = "…"),
                shown.passage.length <= MAX_SHOWN,
                shown.passage.substring(range = shown.highlight)
            )
        )
    }

    // Audit: a click opened the closest of two notes, yet its Linked tab left the link out, as resolving found no single one.
    @Test
    fun `a link to a name two notes share is linked to the note a click opens, and only to that one`() {
        val shared = "Recipes/Arepas.md"
        val root = "Arepas.md"
        val source = RelativePath(value = "Travel/Lisbon.md")
        val texts = mapOf(source to NoteText(body = "Try [[Arepas]].", folded = "try [[arepas]]."))
        val notes = texts.keys + RelativePath(value = root) + RelativePath(value = shared)

        val linked = listOf(root, shared).map { path ->
            findMentions(note = entry(path = path), notes = notes, texts = texts).linked.size
        }

        assertEquals(expected = listOf(1, 0), actual = linked)
    }

    @Test
    fun `a Markdown link never closed is not a link, and finding mentions goes on`() {
        val mentions = find("Travel/Lisbon.md" to "See [x](<Arepas.md>")

        assertEquals(expected = 0, actual = mentions.linked.size)
    }

    private fun find(
        vararg notes: Pair<String, String>
    ): NoteMentions {
        val texts = notes.associate { (path, body) ->
            RelativePath(value = path) to
                NoteText(body = body, folded = body.lowercase())
        }
        return findMentions(
            note = entry(path = AREPAS, metadata = NoteMetadata.NONE.copy(aliases = listOf("Corn cakes"))),
            notes = texts.keys + RelativePath(value = AREPAS),
            texts = texts
        )
    }

    private fun mention(
        source: String,
        count: Int,
        passage: String,
        lit: String
    ): NoteMention {
        val start = passage.indexOf(string = lit)
        return NoteMention(
            source = RelativePath(value = source),
            range = IntRange.EMPTY,
            count = count,
            passage = passage,
            highlight = start until start + lit.length
        )
    }

    private companion object {
        const val AREPAS = "Recipes/Arepas.md"
        const val MAX_SHOWN = 184
    }
}

package app.notekea.cards.domain

import app.notekea.cards.data.GhostCardsReader
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Reading the deck, answering a card and making cards with the model (4.6), over fakes. */
class StudyCardsUseCasesTest {
    private val store = FakeNoteContentStore()
    private val engine = FakeLlmEngine()
    private val note = RelativePath(value = "Capitals.md")
    private val today = CalendarDate(
        year = 2026,
        month = 11,
        day = 11
    )

    @Test
    fun `the deck is the cards of the notes tagged flashcards`() = runTest {
        store.texts[note] = "#flashcards\nCapital of Peru::Lima"
        store.texts[RelativePath(value = "Other.md")] = "Capital of Chile::Santiago"

        val tagged = entry(path = note.value).let {
            it.copy(metadata = it.metadata.copy(textTags = listOf("flashcards")))
        }

        val deck = ReadDeckUseCase(noteContentStore = store)(
            vaultPath = VAULT,
            notes = listOf(tagged, entry(path = "Other.md"))
        )

        assertEquals(
            expected = listOf("Capital of Peru"),
            actual = deck.map { it.question }
        )
    }

    @Test
    fun `an answer writes the next date on the card's line, found by its question when the note moved on`() =
        runTest {
            store.texts[note] = "#flashcards\nCapital of Peru::Lima"

            val card = CardFormat.cardsOf(
                note = note,
                text = store.texts.getValue(note)
            ).single()

            store.texts[note] = "#flashcards\n\nA new first line\nCapital of Peru::Lima"

            val answered =
                AnswerCardUseCase(noteContentStore = store)(VAULT, card, CardAnswer.GOOD, today)

            assertEquals(
                expected = listOf<Any?>(3, "#flashcards\n\nA new first line\nCapital of Peru::Lima <!--SR:!2026-11-14,3,250-->"),
                actual = listOf(answered?.line, store.texts[note])
            )
        }

    @Test
    fun `a card gone from its note is skipped and nothing is written`() = runTest {
        store.texts[note] = "#flashcards\nCapital of Peru::Lima"

        val card = CardFormat.cardsOf(
            note = note,
            text = store.texts.getValue(note)
        ).single()

        store.texts[note] = "#flashcards\nCapital of Chile::Santiago"

        val answered =
            AnswerCardUseCase(noteContentStore = store)(VAULT, card, CardAnswer.EASY, today)

        assertEquals(
            expected = listOf<Any?>(null, "#flashcards\nCapital of Chile::Santiago"),
            actual = listOf(answered, store.texts[note])
        )
    }

    @Test
    fun `only cards whose sentence is in the note and backs them are kept, and none twice`() =
        runTest {
            store.texts[note] = "# Peru\n\nThe capital of Peru is Lima, on the coast.\n\n#flashcards\nOld question::old"

            engine.reply = {
                "{\"cards\":[" +
                    "{\"question\":\"Capital of Peru?\",\"answer\":\"Lima\",\"sentence\":\"The capital of Peru is Lima\"}," +
                    "{\"question\":\"capital of peru?\",\"answer\":\"Lima\",\"sentence\":\"The capital of Peru is Lima\"}," +
                    "{\"question\":\"Its river?\",\"answer\":\"Rímac\",\"sentence\":\"Lima lies on the river Rímac\"}," +
                    "{\"question\":\"Old question\",\"answer\":\"old\",\"sentence\":\"The capital of Peru is Lima\"}]}"
            }

            engine.scoreOf = { query, _ ->
                if ("Lima" in query) 2f else -8f
            }

            val made = MakeCardsUseCase(
                llmEngine = engine,
                reader = GhostCardsReader(),
                noteContentStore = store
            )(
                vaultPath = VAULT,
                note = note
            )

            assertEquals(
                expected = listOf("Capital of Peru?"),
                actual = (made as Outcome.Success).value.map { it.question }
            )
        }

    @Test
    fun `cards none of which the note backs are a problem, not a card`() = runTest {
        store.texts[note] = "# Peru\n\nThe capital of Peru is Lima, on the coast."
        engine.reply = { "{\"cards\":[{\"question\":\"Its river?\",\"answer\":\"Rímac\",\"sentence\":\"Rímac\"}]}" }

        val made = MakeCardsUseCase(
            llmEngine = engine,
            reader = GhostCardsReader(),
            noteContentStore = store
        )(
            vaultPath = VAULT,
            note = note
        )

        assertEquals(
            expected = Outcome.Failure(error = CardsProblem.NONE_BACKED),
            actual = made
        )
    }
}

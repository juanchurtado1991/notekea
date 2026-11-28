package app.notekea.cards.domain

import app.notekea.cards.data.GhostCardsReader
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A hard card written anew from its sentence and, when chosen, replaced in its note (plan step 4), over fakes. */
class RewriteCardsTest {
    private val store = FakeNoteContentStore()
    private val engine = FakeLlmEngine()
    private val note = RelativePath(value = "Capitals.md")
    private val sentence = "The capital of Peru is Lima, on the coast."

    private val hard = StudyCard(
        note = note,
        line = 1,
        question = "Capital of Peru",
        answer = "Lima",
        due = CalendarDate(
            year = 2026,
            month = 11,
            day = 20
        ),
        interval = 1,
        ease = CardSchedule.STRUGGLING_EASE,
        source = sentence
    )

    @Test
    fun `a card is hard once its ease fell four Hard answers, never while new`() {
        assertEquals(
            expected = listOf(true, false, false),
            actual = listOf(
                hard.isStruggling,
                hard.copy(ease = CardSchedule.STRUGGLING_EASE + 20).isStruggling,
                hard.copy(due = null).isStruggling
            )
        )
    }

    @Test
    fun `the first wording that asks something new and its sentence backs is proposed`() = runTest {
        engine.reply = {
            "{\"cards\":[" +
                "{\"question\":\"capital of Peru\",\"answer\":\"Lima\"}," +
                "{\"question\":\"Peru's people?\",\"answer\":\"10 million\"}," +
                "{\"question\":\"Which river?\",\"answer\":\"Rímac\"}," +
                "{\"question\":\"Which city is Peru's capital?\",\"answer\":\"Lima\"}]}"
        }

        engine.scoreOf = { query, _ ->
            if ("Lima" in query || "million" in query) 2f else -8f
        }

        val rewrite = RewriteCardUseCase(
            llmEngine = engine,
            reader = GhostCardsReader()
        )(card = hard)

        assertEquals(
            expected = ProposedCard(
                question = "Which city is Peru's capital?",
                answer = "Lima",
                sentence = sentence
            ),
            actual = rewrite
        )
    }

    @Test
    fun `no wording is asked for a card without its sentence, one the note dropped, or with no model`() =
        runTest {
            engine.reply = { "{\"cards\":[{\"question\":\"Which city is Peru's capital?\",\"answer\":\"Lima\"}]}" }

            val rewriteCard = RewriteCardUseCase(
                llmEngine = engine,
                reader = GhostCardsReader()
            )

            val unsourced = rewriteCard(card = hard.copy(source = null))
            val outdated = rewriteCard(card = hard.copy(isOutdated = true))
            engine.readiness = Outcome.Failure(error = LlmError.NoModel)
            val unready = rewriteCard(card = hard)

            assertEquals(
                expected = listOf<Any?>(null, null, null, 0),
                actual = listOf(unsourced, outdated, unready, engine.generated.size)
            )
        }

    @Test
    fun `the chosen wording replaces the card on its line, which starts again as new without its schedule`() =
        runTest {
            store.texts[note] =
                "#flashcards\n- Capital of Peru::Lima <!--source: $sentence-->\n<!--SR:!2026-11-20,1,170-->\nNext"

            val replaced = ReplaceCardUseCase(noteContentStore = store)(
                vaultPath = VAULT,
                card = hard,
                with = ProposedCard(
                    question = "Which city is Peru's capital?",
                    answer = "Lima",
                    sentence = sentence
                )
            )

            assertEquals(
                expected = listOf<Any?>(
                    "#flashcards\n- Which city is Peru's capital?::Lima <!--source: $sentence-->\nNext",
                    true,
                    sentence,
                    "#flashcards\n- Capital of Peru::Lima <!--source: $sentence-->\n<!--SR:!2026-11-20,1,170-->\nNext"
                ),
                actual = listOf(
                    store.texts[note],
                    replaced?.card?.isNew,
                    replaced?.card?.source,
                    replaced?.original
                )
            )
        }
}

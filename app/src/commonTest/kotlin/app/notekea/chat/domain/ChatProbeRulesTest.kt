package app.notekea.chat.domain

import app.notekea.chat.domain.answer.Answer
import app.notekea.chat.domain.answer.PropertyPassages
import app.notekea.chat.domain.answer.QuestionKept
import app.notekea.chat.domain.answer.RefusalChecked
import app.notekea.chat.domain.answer.SumsChecked
import app.notekea.chat.domain.language.ReplyReworded
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.chat.domain.language.inLanguage
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.plan.ThemeWords
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.chat.domain.prompt.ConversePrompt
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the chat probe of 2026-11-11 found, each as the rule that fixes it: the reply's language, a question kept a
 * question, a reply reworded, a dark theme, and a move into a folder.
 */
class ChatProbeRulesTest {
    private val history = listOf(
        ChatExchange(
            question = "What is Arepas about?",
            reply = "A recipe."
        )
    )

    @Test
    fun `the language is the message's, or the one it asks for by name`() {
        assertEquals(
            expected = listOf("Spanish", "French", "English", "Spanish", "English", null),
            actual = listOf(
                SpokenLanguage.of(text = "quien es Ana y en que proyectos esta?"),
                SpokenLanguage.of(text = "je voudrais savoir ce que j'ai écrit sur Lisbonne"),
                SpokenLanguage.of(text = "How much water do the arepas need?"),
                SpokenLanguage.of(text = "in spanish please"),
                SpokenLanguage.of(text = "contestame en inglés"),
                SpokenLanguage.of(text = "asdkjh qwe zzz")
            )
        )
    }

    @Test
    fun `the person's language is said at the end of the last message, and nothing when unknown`() {
        val messages = listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = "Answer."
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "Who is Ana?"
            )
        )

        assertEquals(
            expected = listOf("Who is Ana?\n\nWrite your reply in Spanish, every word of it.", "Who is Ana?"),
            actual = listOf(
                messages
                    .inLanguage(language = "Spanish")
                    .last()
                    .text,
                messages
                    .inLanguage(language = null)
                    .last()
                    .text
            )
        )
    }

    @Test
    fun `a question planned as a new note or as opening a note is answered, unless writing is asked`() {
        val note = listOf(
            PlannedStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = listOf("Deep Work")
            )
        )

        val opened = listOf(
            PlannedStep(
                kind = ActionKind.OPEN_NOTE,
                fields = mapOf(ActionField.NOTE to "2026-10-22")
            )
        )

        assertEquals(
            expected = listOf(
                ActionKind.ANSWER,
                ActionKind.WRITE_FROM_NOTES,
                ActionKind.ANSWER,
                ActionKind.OPEN_NOTE
            ),
            actual = listOf(
                QuestionKept.shaped(
                    steps = note,
                    request = "compare Deep Work and Atomic Habits"
                ),
                QuestionKept.shaped(
                    steps = note,
                    request = "write a note comparing Deep Work and Atomic Habits"
                ),
                QuestionKept.shaped(
                    steps = opened,
                    request = "what did I write on October 22?"
                ),
                QuestionKept.shaped(
                    steps = opened,
                    request = "can you open the note of October 22?"
                )
            ).map { it.single().kind }
        )
    }

    @Test
    fun `make it shorter rewords the last reply, and only when there is one`() {
        val answer = listOf(PlannedStep(kind = ActionKind.ANSWER))

        assertEquals(
            expected = listOf(ActionKind.CHAT, ActionKind.CHAT, ActionKind.ANSWER),
            actual = listOf(
                ReplyReworded.shaped(
                    steps = answer,
                    request = "make it shorter",
                    history = history
                ),
                ReplyReworded.shaped(
                    steps = answer,
                    request = "ponelo en viñetas",
                    history = history
                ),
                ReplyReworded.shaped(
                    steps = answer,
                    request = "make it shorter",
                    history = emptyList()
                )
            ).map { it.single().kind }
        )
    }

    @Test
    fun `a theme left out is the dark or light mode the request names`() {
        val theme = PlannedStep(kind = ActionKind.SET_THEME)

        assertEquals(
            expected = listOf("dark", "light", null),
            actual = listOf(
                ThemeWords.shaped(
                    step = theme,
                    request = "cambia el tema a oscuro"
                )[ActionField.THEME],
                ThemeWords.shaped(
                    step = theme,
                    request = "switch to light mode"
                )[ActionField.THEME],
                ThemeWords.shaped(
                    step = theme,
                    request = "change the theme"
                )[ActionField.THEME]
            )
        )
    }

    @Test
    fun `named notes moved with a folder but no target go into that folder`() {
        val notes = listOf(entry(path = "Recipes/Pancakes.md"), entry(path = "Archive/Old.md"))

        val moved = CheckPlanUseCase()(
            steps = listOf(
                PlannedStep(
                    kind = ActionKind.MOVE,
                    fields = mapOf(ActionField.FOLDER to "Archive"),
                    notes = listOf("Pancakes")
                )
            ),
            notes = notes,
            openNote = null
        ) as app.notekea.core.domain.Outcome.Success

        val step = moved.value.single()

        assertEquals(
            expected = listOf("Recipes/Pancakes.md") to "Archive",
            actual = step.notes.map { it.value } to step.target?.value
        )
    }

    @Test
    fun `sums written out get their real result, and dates are left alone`() {
        assertEquals(
            expected = listOf(
                "El presupuesto total es de 7050 (4500 + 300 + 2000 + 250).",
                "Spent 0 + 120 + 450 + 190 = 760.",
                "The kickoff, 3 (2026-10-21), is done."
            ),
            actual = listOf(
                SumsChecked.corrected(text = "El presupuesto total es de 9250 (4500 + 300 + 2000 + 250)."),
                SumsChecked.corrected(text = "Spent 0 + 120 + 450 + 190 = 700."),
                SumsChecked.corrected(text = "The kickoff, 3 (2026-10-21), is done.")
            )
        )
    }

    @Test
    fun `a refusal of what Notekea can do is unfounded, one of what it cannot is kept`() {
        val refused = listOf(PlannedStep(kind = ActionKind.REFUSE))

        assertEquals(
            expected = listOf(true, true, false, false, false),
            actual = listOf(
                RefusalChecked.isUnfounded(
                    steps = refused,
                    request = "and how long does the dough rest?"
                ),
                RefusalChecked.isUnfounded(
                    steps = refused,
                    request = "mové la nota de pancakes a Desayunos"
                ),
                RefusalChecked.isUnfounded(
                    steps = refused,
                    request = "mandame un recordatorio mañana"
                ),
                RefusalChecked.isUnfounded(
                    steps = refused,
                    request = "what's the weather in Lisbon?"
                ),
                RefusalChecked.isUnfounded(
                    steps = listOf(PlannedStep(kind = ActionKind.ANSWER)),
                    request = "hi"
                )
            )
        )
    }

    @Test
    fun `the properties of the notes a question names are passages to answer from`() {
        val launch = entry(
            path = "Projects/Notekea launch.md",
            metadata = NoteMetadata.NONE.copy(
                properties = listOf(
                    NoteProperty(
                        key = "owner",
                        value = PropertyValue.Text(text = "Ana")
                    ),
                    NoteProperty(
                        key = "due",
                        value = PropertyValue.Text(text = "2026-11-15")
                    )
                )
            )
        )

        val dune = entry(
            path = "Books/Dune.md",
            metadata = NoteMetadata.NONE.copy(
                properties = listOf(
                    NoteProperty(
                        key = "rating",
                        value = PropertyValue.Number(text = "5")
                    )
                )
            )
        )

        val notes = listOf(launch, dune, entry(path = "Recipes/Arepas.md"))

        assertEquals(
            expected = listOf(
                listOf("Notekea launch — owner: Ana; due: 2026-11-15"),
                listOf("Dune — rating: 5"),
                emptyList()
            ),
            actual = listOf(
                PropertyPassages.of(
                    question = "who owns the Notekea launch?",
                    notes = notes,
                    only = null
                ),
                PropertyPassages.of(
                    question = "which books did I rate 5?",
                    notes = notes,
                    only = null
                ),
                PropertyPassages.of(
                    question = "how do I make arepas?",
                    notes = notes,
                    only = null
                )
            ).map { passages ->
                passages.map { it.text }
            }
        )
    }

    @Test
    fun `a move goes into a new folder, which it makes`() {
        val moved = CheckPlanUseCase()(
            steps = listOf(
                PlannedStep(
                    kind = ActionKind.MOVE,
                    fields = mapOf(ActionField.NOTE to "Pancakes", ActionField.TARGET to "Desayunos")
                )
            ),
            notes = listOf(entry(path = "Recipes/Pancakes.md")),
            openNote = null
        ) as app.notekea.core.domain.Outcome.Success

        assertEquals(
            expected = "Desayunos",
            actual = moved
                .value
                .single()
                .target
                ?.value
        )
    }

    @Test
    fun `a poem or a joke is written from the notes, a question is answered in a few sentences`() {
        val poem = AnswerPrompt.messages(
            question = "escribe un poema sobre mis recetas",
            excerpts = listOf("Arepas")
        )

        val asked = AnswerPrompt.messages(
            question = "how much water?",
            excerpts = listOf("Arepas")
        )

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(poem, asked).map {
                it
                    .first()
                    .text
                    .startsWith("Write what the person asks for")
            }
        )
    }

    @Test
    fun `the talk knows today's date`() {
        val talk = ConversePrompt.messages(
            message = "¿qué día es hoy?",
            history = emptyList(),
            note = null,
            today = "2026-11-11"
        )

        assertEquals(
            expected = true,
            actual = talk
                .first()
                .text
                .endsWith("Today is 2026-11-11.")
        )
    }
}

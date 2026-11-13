package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.core.notes.domain.RelativePath

/** A chat as the previews show it: an answered question with its sources, and one being written. */
internal object ChatPreviewData {
    private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
    private val DEEP_WORK = RelativePath(value = "Books/Deep Work.md")

    val sources = listOf(
        AnswerSource(
            number = 1,
            note = AREPAS,
            headingPath = listOf("Arepas", "Ingredients"),
            range = 0..60,
            text = "2 cups of pre-cooked corn flour, 2½ cups of warm water, 1 teaspoon of salt."
        ),
        AnswerSource(
            number = 2,
            note = AREPAS,
            headingPath = listOf("Arepas", "Steps"),
            range = 61..120,
            text = "Mix the flour, the water and the salt, then let it rest five minutes."
        )
    )

    val answered = ChatEntry(
        id = 1,
        question = "How do I make the arepa dough?",
        scope = ChatScope.VAULT,
        step = ChatStep.DONE,
        answer = "Mix the flour with warm water and salt [1], then rest it five minutes [2].",
        sources = sources,
        readNotes = 3,
        scopeName = null,
        outcome = TurnOutcome.ANSWERED,
        error = null
    )

    val writing = answered.copy(
        id = 2,
        question = "¿Cuánto reposa la masa?",
        step = ChatStep.WRITING,
        answer = "Reposa cinco minutos [2]",
        outcome = null
    )

    /** A note to write from two books, on its card. */
    val proposed = ChatAction(
        status = ActionStatus.PROPOSED,
        steps = listOf(
            ActionStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = listOf(DEEP_WORK, RelativePath(value = "Books/Atomic Habits.md")),
                title = "Focus and habits",
                content = listOf(
                    NoteDraft(
                        path = RelativePath(value = "Books/Focus and habits.md"),
                        text = "# Focus and habits\n\n## Deep work\n- Long blocks of focus [[Deep Work]]"
                    )
                )
            ),
            ActionStep(
                kind = ActionKind.ADD_TAG,
                notes = listOf(AREPAS),
                tag = "food"
            )
        )
    )

    /** The books rated 4 or more, as the chat finds them. */
    val found = ChatAction(
        status = ActionStatus.FINISHED,
        found = listOf(
            FoundNotes(
                columns = listOf("rating"),
                rows = listOf(
                    FoundRow(
                        note = RelativePath(value = "Books/Dune.md"),
                        cells = listOf("5")
                    ),
                    FoundRow(
                        note = RelativePath(value = "Books/Deep Work.md"),
                        cells = listOf("4")
                    )
                )
            )
        )
    )

    val state = ChatState(
        entries = listOf(answered, writing),
        question = "",
        model = ModelStatus.READY,
        isIndexReady = true,
        noteName = "Arepas",
        folderName = "Recipes"
    )
}

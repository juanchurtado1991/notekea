package app.notekea.chat.presentation

import app.notekea.chat.domain.AnswerSource
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.TurnOutcome
import app.notekea.core.notes.domain.RelativePath

/** A chat as the previews show it: an answered question with its sources, and one being written. */
internal object ChatPreviewData {
    private val AREPAS = RelativePath(value = "Recipes/Arepas.md")

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

    val state = ChatState(
        entries = listOf(answered, writing),
        question = "",
        model = ModelStatus.READY,
        isIndexReady = true,
        noteName = "Arepas",
        folderName = "Recipes"
    )
}

package app.notekea.chat.domain.answer

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep

/**
 * A question stays a question when the model plans something else (the chat probe, 2026-11-11): a new note, opening a
 * note, a search ("what did I cook on October 22?"). It is answered from the notes; writing or a search the request
 * asks for ("write a note comparing…", "which notes link to…") is kept.
 */
internal object QuestionKept {
    private val WRITTEN = setOf(ActionKind.CREATE_NOTE, ActionKind.WRITE_FROM_NOTES)
    private val OPENED = setOf(ActionKind.OPEN_NOTE, ActionKind.OPEN_TODAY)

    // "je voudrais savoir ce que j'ai écrit sur Lisbonne": what was written, asked without a question word.
    private val WROTE = Regex(
        pattern = "(ce que j'ai écrit|lo que escrib\\w*|what I wrote|what I have written|o que escrevi)",
        option = RegexOption.IGNORE_CASE
    )

    private val NOTES_ASKED = Regex(
        pattern = "\\b(notes?|notas?|links?|link to|enlaza\\w*|mention\\w*|menciona\\w*|find|busca\\w*|search)\\b",
        option = RegexOption.IGNORE_CASE
    )

    fun shaped(
        steps: List<PlannedStep>,
        request: String
    ): List<PlannedStep> {
        // A plan may answer beside the action ("open_note, answer"): the answer does not keep the action.
        val kinds = steps.map { it.kind }.toSet() - ActionKind.ANSWER

        val isWrittenUnasked =
            kinds.isNotEmpty() &&
                kinds.all { it in WRITTEN } &&
                !AskedWords.asksForWriting(request)

        val isOpenedAsked =
            kinds.isNotEmpty() &&
                kinds.all { it in OPENED } &&
                AskedWords.asksAbout(request)

        val isSearchedAsked =
            kinds == setOf(ActionKind.SEARCH) &&
                (AskedWords.asksAbout(request) || WROTE.containsMatchIn(input = request)) &&
                !NOTES_ASKED.containsMatchIn(input = request)

        val isAnswered =
            isWrittenUnasked ||
                isOpenedAsked ||
                isSearchedAsked

        if (!isAnswered) return steps

        return listOf(
            PlannedStep(
                kind = ActionKind.ANSWER,
                fields = mapOf(ActionField.REQUEST to request)
            )
        )
    }
}
